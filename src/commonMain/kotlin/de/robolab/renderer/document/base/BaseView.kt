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

abstract class BaseView(
        override val tag: String? = null
) : IView {

    val animatableManager = AnimatableManager()

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
        if (element.parent != null) {
            throw IllegalStateException("View $element already has a parent (${element.parent}) and cannot be added to $this")
        }
        viewList.add(index, element)

        element.document = document
        element.parent = this

        callOnCreate(element)

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

    override fun removeAt(index: Int): IView {
        val view = viewList[index]

        callOnDestroy(view) {
            viewList.remove(view)

            view.parent = null
            view.document = null

            animatableManager.unregisterAnimatable(view)
            requestRedraw()
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

        updateBoundingBox()

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

    override val onAnimationStart = animatableManager.onAnimationStart
    override val onAnimationFinish = animatableManager.onAnimationFinish

    override val onPointerDown = EventHandler<PointerEvent>()
    override val onPointerUp = EventHandler<PointerEvent>()
    override val onPointerMove = EventHandler<PointerEvent>()
    override val onPointerDrag = EventHandler<PointerEvent>()
    override val onPointerSecondaryAction = EventHandler<PointerEvent>()
    override val onKeyPress = EventHandler<KeyEvent>()
    override val onKeyRelease = EventHandler<KeyEvent>()
}
