package de.robolab.client.renderer.view.base

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.transition.*
import de.robolab.common.utils.*
import de.westermann.kobserve.event.EventHandler

abstract class BaseView(
    override val tag: String? = null
) : IView {

    val animatableManager = AnimatableManager()

    @Suppress("UNUSED_PARAMETER")
    override var enabled: Boolean
        get() = document != null
        set(value) {
        }

    override val isRunning: Boolean
        get() = animatableManager.isRunning

    override fun requestRedraw() {
        animatableManager.requestRedraw()
    }

    private val transitionList = mutableListOf<GenericTransition<*>>()

    private fun registerTransition(transition: GenericTransition<*>) {
        transitionList += transition

        animatableManager.registerAnimatable(transition)

        transition.onChange {
            updateBoundingBox()
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


    private val viewList = mutableListOf<IView>()

    protected open fun callOnCreate(view: IView) {
        view.onCreate()

        for (child in view) {
            callOnCreate(child)
        }

        if (view is BaseView) {
            view.boundingBox = view.calculateBoundingBox()
        }
    }

    override fun add(index: Int, element: IView) {
        val oldParent = element.parent
        if (oldParent != null) {
            if (oldParent == this) {
                removeTodo.remove(element)
                callOnCreate(element)
                updateBoundingBox()
                return
            }
            if (oldParent is BaseView) {
                oldParent.finishRemove(element)
            } else {
                throw IllegalStateException("View $element already has a parent (${element.parent}) and cannot be added to $this")
            }
        }
        viewList.add(index, element)

        element.document = document
        element.parent = this

        callOnCreate(element)
        updateBoundingBox()

        animatableManager.registerAnimatable(element)
        requestRedraw()
    }

    private fun callOnDestroy(view: IView, callback: () -> Unit) {
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

    private val removeTodo = mutableListOf<IView>()
    fun finishRemove(view: IView) {
        if (!removeTodo.remove(view)) {
            return
        }

        viewList.remove(view)

        view.parent = null
        view.document = null

        updateBoundingBox()
        animatableManager.unregisterAnimatable(view)
        requestRedraw()
    }

    override fun removeAt(index: Int): IView {
        val view = viewList[index]

        if (view.isFocused) {
            view.blur()
        }

        removeTodo.add(view)
        callOnDestroy(view) {
            finishRemove(view)
        }

        return view
    }

    override fun clear() {
        for (index in lastIndex downTo 0) {
            removeAt(index)
        }
    }

    override val size: Int
        get() = viewList.size

    override fun contains(element: IView): Boolean {
        return element in viewList
    }

    override fun get(index: Int): IView {
        return viewList[index]
    }

    override fun indexOf(element: IView): Int {
        return viewList.indexOf(element)
    }

    override fun lastIndexOf(element: IView): Int {
        return viewList.lastIndexOf(element)
    }

    override fun listIterator(index: Int): MutableListIterator<IView> {
        return object : MutableListIterator<IView> {

            private val iterator: MutableListIterator<IView> = viewList.listIterator()

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): IView {
                return iterator.next()
            }

            override fun remove() {
                throw UnsupportedOperationException()
            }

            override fun hasPrevious(): Boolean {
                return iterator.hasPrevious()
            }

            override fun nextIndex(): Int {
                return iterator.nextIndex()
            }

            override fun previous(): IView {
                return iterator.previous()
            }

            override fun previousIndex(): Int {
                return iterator.previousIndex()
            }

            override fun add(element: IView) {
                throw UnsupportedOperationException()
            }

            override fun set(element: IView) {
                throw UnsupportedOperationException()
            }
        }
    }

    open fun calculateBoundingBox(): Rectangle? {
        val rects = map { it.boundingBox }

        if (rects.isEmpty()) {
            return null
        }

        return rects.reduce { acc, rectangle -> acc unionNullable rectangle }
    }

    private fun updateBoundingBox() {
        val box = calculateBoundingBox()

        if (box != boundingBox) {
            boundingBox = box

            (parent as? BaseView)?.updateBoundingBox()
        }
    }

    final override var boundingBox: Rectangle? = null

    override fun onUpdate(msOffset: Double): Boolean {
        val hasChanged = animatableManager.onUpdate(msOffset)

        if (hasChanged) {
            updateBoundingBox()
        }

        return hasChanged
    }

    override fun onDraw(context: DrawContext) {
        for (box in viewList) {
            val bounding = box.boundingBox

            if (bounding == null || context.area intersects bounding) {
                box.onDraw(context)
            }
        }
    }

    override fun onDebugDraw(context: DrawContext) {
        context.renderedViewCount += 1
        val box = boundingBox
        if (box != null) {
            val color = when {
                isHovered -> Color.LIME
                isRunning -> Color.AQUA
                else -> Color.RED
            }
            val width = (if (isHovered) 2.0 else 1.0) / context.transformation.scaledGridWidth

            context.strokeRect(box, color, width)

            val s = 0.02
            context.strokeLine(
                listOf(
                    Point(box.left + s, box.top),
                    Point(box.left, box.top + s)
                ), color, width
            )
            context.strokeLine(
                listOf(
                    Point(box.left + s, box.bottom),
                    Point(box.left, box.bottom - s)
                ), color, width
            )
            context.strokeLine(
                listOf(
                    Point(box.right - s, box.top),
                    Point(box.right, box.top + s)
                ), color, width
            )
            context.strokeLine(
                listOf(
                    Point(box.right - s, box.bottom),
                    Point(box.right, box.bottom - s)
                ), color, width
            )
        }

        for (view in viewList) {
            val bounding = view.boundingBox

            if (bounding == null || context.area intersects bounding) {
                view.onDebugDraw(context)
            }
        }
    }


    override var parent: IView? = null

    override var document: Document? = null
        set(value) {
            field = value
            animatableManager.enabled = value != null
            for (view in viewList) {
                view.document = value
            }
        }

    private var internalAnimationTime: Double? = null
    override var animationTime: Double
        get() = internalAnimationTime ?: parent?.animationTime ?: 0.0
        set(value) {
            internalAnimationTime = value
        }

    fun resetAnimationTime() {
        internalAnimationTime = null
    }

    fun animateImmediately(block: () -> Unit) {
        val old = internalAnimationTime
        internalAnimationTime = 0.0
        block()
        internalAnimationTime = old
    }

    override var focusable = false
        set(value) {
            if (!value && isFocused) {
                blur()
            }
            field = value
        }

    override var hoverable = true

    open fun debugStringParameter(): List<Any?> {
        return emptyList()
    }

    override fun toString(): String {
        val params = if (tag == null) debugStringParameter() else listOf(tag) + debugStringParameter()

        val name = this::class.simpleName ?: return ""
        return if (params.isEmpty()) name else "$name(${params.joinToString(", ")})"
    }

    override val onFocus = EventHandler<Unit>()
    override val onBlur = EventHandler<Unit>()

    override val onHoverEnter = EventHandler<Unit>()
    override val onHoverLeave = EventHandler<Unit>()

    override val onAnimationStart = animatableManager.onAnimationStart
    override val onAnimationFinish = animatableManager.onAnimationFinish

    override val onPointerDown = EventHandler<PointerEvent>()
    override val onPointerUp = EventHandler<PointerEvent>()
    override val onPointerMove = EventHandler<PointerEvent>()
    override val onPointerDrag = EventHandler<PointerEvent>()
    override val onPointerSecondaryAction = EventHandler<PointerEvent>()
    override val onKeyPress = EventHandler<KeyEvent>()
    override val onKeyRelease = EventHandler<KeyEvent>()

    override val onCanvasResize = EventHandler<Dimension>()
    override val onUserTransformation = EventHandler<Unit>()

    private val extra = mutableMapOf<String, Any>()

    override fun extraPut(key: String, value: Any) {
        extra[key] = value
    }

    override fun extraGet(key: String): Any? {
        return extra[key]
    }
}
