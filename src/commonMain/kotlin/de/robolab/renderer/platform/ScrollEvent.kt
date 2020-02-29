package de.robolab.renderer.platform

import de.robolab.renderer.data.Point

data class ScrollEvent(
        val point: Point,
        val delta: Point,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
)