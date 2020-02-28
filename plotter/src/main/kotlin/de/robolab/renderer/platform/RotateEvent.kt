package de.robolab.renderer.platform

import de.robolab.renderer.data.Point

data class RotateEvent(
        val point: Point,
        val angle: Double,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
)