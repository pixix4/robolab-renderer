package de.robolab.renderer.platform

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point

data class PointerEvent(
        val point: Point,
        val screen: Dimension,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
) {
    var hasMoved: Boolean = false
}