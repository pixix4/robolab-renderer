package de.robolab.renderer.platform

import de.robolab.renderer.data.Point

data class MouseEvent(
        val point: Point,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
)