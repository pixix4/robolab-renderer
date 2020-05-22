package de.robolab.client.renderer.utils

import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point

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

    fun onKeyRelease(event: KeyEvent): Boolean {
        return false
    }

    fun onResize(size: Dimension) {}
    fun onUserTransformation() {}
}
