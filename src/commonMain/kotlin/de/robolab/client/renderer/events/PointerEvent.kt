package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point

data class PointerEvent(
    val mousePoint: Point,
    val screen: Dimension,
    val ctrlKey: Boolean = false,
    val altKey: Boolean = false,
    val shiftKey: Boolean = false
): Event() {
    var planetPoint: Point = mousePoint
    var hasMoved: Boolean = false
}
