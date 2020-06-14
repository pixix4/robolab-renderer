package de.robolab.common.planet

import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.common.utils.Point

data class StartPoint(
    val point: Coordinate,
    val orientation: Direction,
    val controlPoints: List<Point>
): IPlanetValue {
    val path = Path(
        point,
        orientation.opposite(),
        point,
        orientation.opposite(),
        null,
        emptySet(),
        controlPoints,
        hidden = false,
        showDirectionArrow = true
    )

    fun equalPoint(other: StartPoint) = point == other.point && orientation == other.orientation

    fun translate(delta: Coordinate) = StartPoint(
        point.translate(delta),
        orientation,
        controlPoints.map { it + delta.toPoint() }
    )

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate) = StartPoint(
        point.rotate(direction, origin),
        orientation.rotate(direction),
        controlPoints.map { it.rotate(direction.angle, origin.toPoint()) }
    )
}
