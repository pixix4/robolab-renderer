package de.robolab.renderer.platform

import de.robolab.renderer.data.Dimension

interface ICanvasListener {

    fun onPointerDown(event: PointerEvent): Boolean {
        return false
    }

    fun onPointerUp(event: PointerEvent): Boolean {
        return false
    }

    fun onPointerMove(event: PointerEvent): Boolean {
        return false
    }

    fun onPointerDrag(event: PointerEvent): Boolean {
        return false
    }

    fun onPointerSecondaryAction(event: PointerEvent): Boolean {
        return false
    }

    fun onPointerEnter(event: PointerEvent): Boolean {
        return false
    }

    fun onPointerLeave(event: PointerEvent): Boolean {
        return false
    }

    fun onScroll(event: ScrollEvent): Boolean {
        return false
    }

    fun onZoom(event: ZoomEvent): Boolean {
        return false
    }

    fun onRotate(event: RotateEvent): Boolean {
        return false
    }

    fun onResize(size: Dimension): Boolean {
        return false
    }

    fun onKeyPress(event: KeyEvent): Boolean {
        return false
    }
}
