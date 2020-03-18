package de.robolab.renderer.platform

import de.robolab.renderer.data.Dimension

interface ICanvasListener {

    fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    fun onMouseDown(event: MouseEvent): Boolean {
        return false
    }

    fun onMouseUp(event: MouseEvent): Boolean {
        return false
    }

    fun onMouseMove(event: MouseEvent): Boolean {
        return false
    }

    fun onMouseDrag(event: MouseEvent): Boolean {
        return false
    }

    fun onMouseClick(event: MouseEvent): Boolean {
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

    fun onKeyDown(event: KeyEvent): Boolean {
        return false
    }

    fun onKeyUp(event: KeyEvent): Boolean {
        return false
    }
}
