package de.robolab.renderer.platform

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point

data class PointerEvent(
        val mousePoint: Point,
        val screen: Dimension,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
): Event() {
    var canvasPoint: Point = mousePoint
    var hasMoved: Boolean = false
}
