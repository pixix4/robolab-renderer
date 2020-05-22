package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point

data class RotateEvent(
    val point: Point,
    val angle: Double,
    val screen: Dimension,
    val ctrlKey: Boolean = false,
    val altKey: Boolean = false,
    val shiftKey: Boolean = false
)