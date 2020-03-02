package de.robolab.renderer

import de.robolab.renderer.data.Point

data class Pointer(
        val position: Point = Point.ZERO,
        val objectUnderPointer: Any? = null
)
