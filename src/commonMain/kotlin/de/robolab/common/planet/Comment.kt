package de.robolab.common.planet

import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.common.utils.Point

data class Comment(
    val point: Point,
    val alignment: Alignment,
    val lines: List<String>
): IPlanetValue {

    enum class Alignment {
        LEFT, CENTER, RIGHT
    }

    fun translate(delta: Coordinate) = copy(
        point = point + delta.toPoint()
    )

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate) = copy(
        point = point.rotate(direction.angle, origin.toPoint())
    )
}
