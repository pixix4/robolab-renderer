package de.robolab.renderer.platform

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point

data class RotateEvent(
        val point: Point,
        val angle: Double,
        val screen: Dimension,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
)