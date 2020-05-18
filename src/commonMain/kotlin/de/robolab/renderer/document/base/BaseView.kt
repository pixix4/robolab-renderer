package de.robolab.renderer.document.base

import de.robolab.renderer.animation.*
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.emit

abstract class BaseView(
        override val tag: String? = null
) : IView {

    override var isRunning = false

    private var forceRedraw = false
    override fun requestRedraw() {
        forceRedraw = true
        checkAnimationStatus()
    }

    private fun checkAnimationStatus() {
        if (!isRunning && (forceRedraw || transitionList.isNotEmpty() || viewUpdateList.isNotEmpty())) {
            isRunning = true
            onAnimationStart.emit()
        }

        if (isRunning && (!forceRedraw && transitionList.isEmpty() && viewUpdateList.isEmpty())) {
            isRunning = false
            onAnimationFinish.emit()
        }
    }

    private val transitionList = mutableListOf<GenericTransition<*>>()
    private val transitionRemoveList = mutableListOf<GenericTransition<*>>()

    private fun registerTransition(transition: GenericTransition<*>) {
        transition.onAnimationStart {
            transitionRemoveList -= transition
            transitionList += transition

            checkAnimationStatus()
        }
        transition.onAnimationFinish {
            transitionRemoveList += transition
        }
    }

    protected fun transition(initValue: Double): DoubleTransition {
        return DoubleTransition(initValue).also(this::registerTransition)
    }

    protected fun <T : IInterpolatable<T>> transition(initValue: T): ValueTransition<T> {
        return ValueTransition(initValue).also(this::registerTransition)
    }

    protected fun <T> transition(initValue: T, interpolate: (T, T, Double) -> T): GenericTransition<T> {
        return GenericTransition(initValue, interpolate).also(this::registerTransition)
    }

    protected fun transition(initValue: List<Double>): DoubleListTransition {
        return DoubleListTransition(initValue).also(this::registerTransition)
    }

    protected fun <T : IInterpolatable<T>> transition(initValue: List<T>): ValueListTransition<T> {
        return ValueListTransition(initValue).also(this::registerTransition)
    }


    private val viewList = mutableListOf<ViewBox>()
    private val viewUpdateList = mutableListOf<IView>()
    private val viewUpdateRemoveList = mutableListOf<IView>()

    protected open fun callOnCreate(view: IView) {
        view.onCreate()

        for (child in view) {
            callOnCreate(child)
        }

        if (view is BaseView) {
            view.boundingBox = view.updateBoundingBox()
        }
    }

    override fun add(element: IView): Boolean {
        val box = ViewBox(element)
        viewList += box

        box.eventListenerList += box.view.onAnimationStart.reference {
            viewUpdateRemoveList.remove(box.view)
            viewUpdateList.add(box.view)
            checkAnimationStatus()
        }
        box.eventListenerList += box.view.onAnimationFinish.reference {
            viewUpdateRemoveList.add(box.view)
        }

        if (box.view.isRunning) {
            viewUpdateList.add(box.view)
            checkAnimationStatus()
        }

        box.view.document = document
        box.view.parent = this

        callOnCreate(box.view)
        requestRedraw()

        return true
    }

    protected fun callOnDestroy(view: IView, callback: () -> Unit) {
        val children = view.toList()

        var callCount = 0
        val onDestroyCallback = {
            callCount += 1
            if (callCount > children.size) {
                callback()
            }
        }
        view.onDestroy(onDestroyCallback)

        for (child in children) {
            callOnDestroy(child, onDestroyCallback)
        }
    }

    override fun remove(element: IView): Boolean {
        val box = viewList.firstOrNull { it.view == element } ?: return false

        callOnDestroy(box.view) {
            viewList.remove(box)
            viewUpdateList.remove(box.view)

            box.remove()
            box.view.parent = null
            box.view.document = null

            requestRedraw()
        }

        return true
    }

    override fun clear() {
        for (element in toList()) {
            remove(element)
        }
    }

    override val size: Int
        get() = viewList.size

    override fun contains(element: IView): Boolean {
        return viewList.indexOfFirst { it.view == element } >= 0
    }

    override fun iterator(): MutableIterator<IView> {
        return object : MutableIterator<IView> {

            private val iterator: MutableIterator<ViewBox> = viewList.listIterator()
            private var lastElement: ViewBox? = null

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): IView {
                val element = iterator.next()
                lastElement = element
                return element.view
            }

            override fun remove() {
                val element = lastElement ?: return
                remove(element.view)
            }
        }
    }

    open fun updateBoundingBox(): Rectangle? {
        val rects = map { it.boundingBox }

        if (rects.isEmpty()) {
            return null
        }

        return rects.reduce { acc, rectangle -> acc unionNullable rectangle }
    }

    final override var boundingBox: Rectangle? = null

    override fun onUpdate(msOffset: Double): Boolean {
        var hasChanged = forceRedraw

        for (transition in transitionList) {
            hasChanged = transition.onUpdate(msOffset) || hasChanged
        }
        if (transitionRemoveList.isNotEmpty()) {
            transitionList -= transitionRemoveList
            transitionRemoveList.clear()

            checkAnimationStatus()
        }

        for (view in viewUpdateList) {
            hasChanged = view.onUpdate(msOffset) || hasChanged
        }
        if (viewUpdateRemoveList.isNotEmpty()) {
            viewUpdateList -= viewUpdateRemoveList
            viewUpdateRemoveList.clear()

            checkAnimationStatus()
        }

        if (hasChanged) {
            boundingBox = updateBoundingBox()
        }

        return hasChanged
    }

    override fun onDraw(context: DrawContext) {
        if (forceRedraw) {
            forceRedraw = false

            checkAnimationStatus()
        }

        for (box in viewList) {
            val bounding = box.view.boundingBox

            if (bounding == null || context.area intersects bounding) {
                box.view.onDraw(context)
            }
        }

        if (context.debug) {
            context.renderedViewCount += 1
            val box = boundingBox
            if (box != null) {
                val color = if (isHovered) Color.LIME else Color.RED
                val width = (if (isHovered) 2.0 else 1.0) / context.transformation.scaledGridWidth

                context.strokeRect(box, color, width)

                val s = 0.02
                context.strokeLine(listOf(
                        Point(box.left + s, box.top),
                        Point(box.left, box.top + s)
                ), color, width)
                context.strokeLine(listOf(
                        Point(box.left + s, box.bottom),
                        Point(box.left, box.bottom - s)
                ), color, width)
                context.strokeLine(listOf(
                        Point(box.right - s, box.top),
                        Point(box.right, box.top + s)
                ), color, width)
                context.strokeLine(listOf(
                        Point(box.right - s, box.bottom),
                        Point(box.right, box.bottom - s)
                ), color, width)
            }
        }
    }


    override var parent: IView? = null

    override var document: Document? = null
        set(value) {
            field = value
            for (view in viewList) {
                view.view.document = value
            }
        }

    private var internalAnimationTime: Double? = null
    override var animationTime: Double
        get() = internalAnimationTime ?: parent?.animationTime ?: 0.0
        set(value) {
            internalAnimationTime = value
        }

    override var focusable = false
        set(value) {
            if (!value && isFocused) {
                blur()
            }
            field = value
        }

    override fun toString(): String {
        val t = tag
        val name = this::class.simpleName ?: return ""
        return if (t == null) name else "$name($t)"
    }

    override val onFocus = EventHandler<Unit>()
    override val onBlur = EventHandler<Unit>()

    override val onHoverEnter = EventHandler<Unit>()
    override val onHoverLeave = EventHandler<Unit>()

    override val onAnimationStart = EventHandler<Unit>()
    override val onAnimationFinish = EventHandler<Unit>()

    override val onPointerDown = EventHandler<PointerEvent>()
    override val onPointerUp = EventHandler<PointerEvent>()
    override val onPointerMove = EventHandler<PointerEvent>()
    override val onPointerDrag = EventHandler<PointerEvent>()
    override val onPointerSecondaryAction = EventHandler<PointerEvent>()
    override val onKeyPress = EventHandler<KeyEvent>()
    override val onKeyRelease = EventHandler<KeyEvent>()

    private class ViewBox(
            val view: IView
    ) {
        val eventListenerList = mutableListOf<EventListener<*>>()

        fun remove() {
            for (listener in eventListenerList) {
                listener.detach()
            }
            eventListenerList.clear()
        }
    }
}
