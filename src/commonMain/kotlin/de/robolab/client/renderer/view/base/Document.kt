package de.robolab.client.renderer.view.base

import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import kotlin.math.max

class Document() : BaseView() {

    constructor(vararg viewList: IView) : this() {
        for (view in viewList) {
            add(view)
        }
    }

    override var animationTime: Double
        get() = plotter?.animationTime ?: 0.0
        set(value) {
            plotter?.animationTime = value
        }

    override var document: Document? = this

    var plotter: PlotterWindow? = null

    val onAttach = EventHandler<PlotterWindow>()
    val onDetach = EventHandler<PlotterWindow>()
    fun onAttach(plotter: PlotterWindow) {
        this.plotter = plotter

        onAttach.emit(plotter)
    }

    fun onDetach(plotter: PlotterWindow) {
        this.plotter = null

        onDetach.emit(plotter)
    }

    override fun calculateBoundingBox(): Rectangle? {
        return null
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return true
    }

    val hoveredStack = mutableListOf<IView>()

    fun isViewHovered(view: IView) = view in hoveredStack
    fun updateHoveredView(canvasPosition: Point, mousePosition: Point, epsilon: Double) {
        val newHoverView: IView? = findHoveredView(canvasPosition, mousePosition, epsilon)

        val oldStack = hoveredStack.toList()
        val newStack = newHoverView?.getViewStack() ?: emptyList()

        if (oldStack == newStack) return

        hoveredStack.clear()
        hoveredStack.addAll(newStack)

        for (index in 0 until max(oldStack.size, newStack.size)) {
            val ov = oldStack.getOrNull(index)
            val nv = newStack.getOrNull(index)

            if (ov == nv) continue

            ov?.onHoverLeave?.emit()
            nv?.onHoverEnter?.emit()
        }
        requestRedraw()
    }


    val focusedStack = mutableListOf<IView>()

    fun isViewFocused(view: IView) = view in focusedStack
    private fun updateFocus(view: IView?) {
        val oldStack = focusedStack.toList()
        val newStack = view?.getViewStack() ?: emptyList()

        if (oldStack == newStack) return

        focusedStack.clear()
        focusedStack.addAll(newStack)

        for (index in 0 until max(oldStack.size, newStack.size)) {
            val ov = oldStack.getOrNull(index)
            val nv = newStack.getOrNull(index)

            if (ov == nv) continue

            ov?.onBlur?.emit()
            nv?.onFocus?.emit()
        }
        requestRedraw()
    }

    fun focusView(view: IView) {
        updateFocus(view)
    }

    fun blurView(view: IView?) {
        if (view == null) {
            updateFocus(null)
            return
        }

        var found = false
        for (element in focusedStack.asReversed()) {
            if (element == view) {
                found = true
                continue
            }

            if (found && element.focusable) {
                updateFocus(element)
                return
            }
        }
    }

    private var pointerDownPrevented = false
    fun emitOnPointerDown(event: PointerEvent): Boolean {
        pointerDownPrevented = false
        for (view in hoveredStack.asReversed()) {
            view.onPointerDown.emit(event)

            if (!event.bubbles) {
                pointerDownPrevented = true
                return true
            }
        }
        return false
    }

    fun emitOnPointerUp(event: PointerEvent): Boolean {
        if (!event.hasMoved && !pointerDownPrevented) {
            var ignoreDefocus = false
            for (view in hoveredStack.asReversed()) {
                if (view.focusable) {
                    if (view.isFocused) {
                        ignoreDefocus = true
                        break
                    } else {
                        updateFocus(view)
                        return true
                    }
                }
            }
            if (focusedStack.isNotEmpty() && !ignoreDefocus) {
                updateFocus(null)
                return true
            }
        }

        for (view in hoveredStack.asReversed()) {
            view.onPointerUp.emit(event)

            if (!event.bubbles) return true
        }
        return false
    }

    fun emitOnPointerMove(event: PointerEvent): Boolean {
        for (view in hoveredStack.asReversed()) {
            view.onPointerMove.emit(event)

            if (!event.bubbles) return true
        }
        return false
    }

    fun emitOnPointerDrag(event: PointerEvent): Boolean {
        for (view in hoveredStack.asReversed()) {
            view.onPointerDrag.emit(event)

            if (!event.bubbles) return true
        }
        return false
    }

    fun emitOnPointerSecondaryAction(event: PointerEvent): Boolean {
        for (view in hoveredStack.asReversed()) {
            view.onPointerSecondaryAction.emit(event)

            if (!event.bubbles) return true
        }
        return false
    }

    fun emitOnKeyPress(event: KeyEvent): Boolean {
        val viewStack = mutableListOf<IView>()
        viewStack += hoveredStack
        viewStack += focusedStack
        for (view in viewStack.distinct().asReversed()) {
            view.onKeyPress.emit(event)

            if (!event.bubbles) return true
        }
        return false
    }

    fun emitOnKeyRelease(event: KeyEvent): Boolean {
        if (event.keyCode == KeyCode.ESCAPE) {
            val focusedStack = focusedStack.toList()
            if (focusedStack.isNotEmpty()) {
                focusedStack.last().blur()

                for (view in focusedStack.dropLast(1).asReversed()) {
                    if (view.focusable) {
                        view.focus()
                        break
                    }
                }

                return true
            }
        }

        val viewStack = mutableListOf<IView>()
        viewStack += hoveredStack
        viewStack += focusedStack
        for (view in viewStack.distinct().asReversed()) {
            view.onKeyRelease.emit(event)

            if (!event.bubbles) return true
        }
        return false
    }

    fun emitOnCanvasResize(event: Dimension) {
        fun emitOnView(view: IView) {
            view.onCanvasResize.emit(event)

            for (v in view) {
                emitOnView(v)
            }
        }

        emitOnView(this)
    }

    fun emitOnUserTransformation() {
        fun emitOnView(view: IView) {
            view.onUserTransformation.emit()

            for (v in view) {
                emitOnView(v)
            }
        }

        emitOnView(this)
    }
}