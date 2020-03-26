package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.platform.*

interface IInteraction {
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

    fun onKeyPress(event: KeyEvent): Boolean {
        return false
    }
    
    fun onResize(size: Dimension) {}
}
