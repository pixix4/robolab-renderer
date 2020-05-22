package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point

data class ScrollEvent(
    val point: Point,
    val delta: Point,
    val screen: Dimension,
    val ctrlKey: Boolean = false,
    val altKey: Boolean = false,
    val shiftKey: Boolean = false
)