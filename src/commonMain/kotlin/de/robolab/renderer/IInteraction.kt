package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.*

interface IInteraction {
    fun onPointerDown(event: PointerEvent, position: Point): Boolean {
        return false
    }

    fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        return false
    }

    fun onPointerMove(event: PointerEvent, position: Point): Boolean {
        return false
    }

    fun onPointerDrag(event: PointerEvent, position: Point): Boolean {
        return false
    }

    fun onPointerSecondaryAction(event: PointerEvent, position: Point): Boolean {
        return false
    }

    fun onKeyPress(event: KeyEvent): Boolean {
        return false
    }
    
    fun onResize(size: Dimension) {}
    fun onUserTransformation() {}
}
