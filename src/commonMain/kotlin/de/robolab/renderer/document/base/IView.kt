package de.robolab.renderer.document.base

import de.robolab.renderer.animation.*
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.EventHandler

interface IView : MutableList<IView>, IAnimatable {

    val tag: String?
    
    fun onDraw(context: DrawContext)
    fun onDebugDraw(context: DrawContext)
    fun requestRedraw()

    val boundingBox: Rectangle?
    fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean

    var document: Document?
    var parent: IView?

    var animationTime: Double

    fun onCreate() {}
    fun onDestroy(onFinish: () -> Unit) {
        onFinish()
    }

    override fun containsAll(elements: Collection<IView>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun add(element: IView): Boolean {
        add(size, element)
        return true
    }

    override fun addAll(index: Int, elements: Collection<IView>): Boolean {
        var i = index
        for (element in elements) {
            add(i++, element)
        }

        return elements.isNotEmpty()
    }

    override fun addAll(elements: Collection<IView>): Boolean {
        return addAll(size, elements)
    }

    override fun remove(element: IView): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<IView>): Boolean {
        var removed = false

        for (element in elements) {
            removed = remove(element) || removed
        }

        return removed
    }

    override fun retainAll(elements: Collection<IView>): Boolean {
        val elementsToRemove = mutableListOf<IView>()

        for (element in this) {
            if (element !in elements) {
                elementsToRemove.add(element)
            }
        }

        return removeAll(elementsToRemove)
    }

    override fun set(index: Int, element: IView): IView {
        val view = removeAt(index)
        add(index, element)
        return view
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<IView> {
        throw UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<IView> {
        return listIterator(0)
    }

    override fun listIterator(): MutableListIterator<IView> {
        return listIterator(0)
    }

    operator fun plusAssign(element: IView) {
        add(element)
    }
    operator fun plusAssign(elements: Collection<IView>) {
        addAll(elements)
    }
    operator fun minusAssign(element: IView) {
        remove(element)
    }
    operator fun minusAssign(elements: Collection<IView>) {
        removeAll(elements)
    }

    var focusable: Boolean
    val isFocused: Boolean
        get() = document?.isViewFocused(this) == true

    val onFocus: EventHandler<Unit>
    val onBlur: EventHandler<Unit>

    fun focus() {
        document?.focusView(this)
    }

    fun blur() {
        document?.blurView(this)
    }

    val isHovered: Boolean
        get() = document?.isViewHovered(this) == true

    val onHoverEnter: EventHandler<Unit>
    val onHoverLeave: EventHandler<Unit>

    val onPointerDown: EventHandler<PointerEvent>
    val onPointerUp: EventHandler<PointerEvent>
    val onPointerMove: EventHandler<PointerEvent>
    val onPointerDrag: EventHandler<PointerEvent>
    val onPointerSecondaryAction: EventHandler<PointerEvent>
    val onKeyPress: EventHandler<KeyEvent>
    val onKeyRelease: EventHandler<KeyEvent>
}

fun IView.getViewStack(): List<IView> {
    val p = parent ?: return listOf(this)
    return p.getViewStack().plusElement(this)
}

fun IView.findHoveredView(planetPoint: Point, canvasPoint: Point, epsilon: Double): IView? {
    for (view in toList().asReversed()) {
        val box = view.boundingBox
        if (box != null && planetPoint !in box) {
            continue
        }

        val hovered = view.findHoveredView(planetPoint, canvasPoint, epsilon)
        if (hovered != null) {
            return hovered
        }
    }

    val box = boundingBox
    if ((box == null || planetPoint in box) && checkPoint(planetPoint, canvasPoint, epsilon)) {
        return this
    }

    return null
}
