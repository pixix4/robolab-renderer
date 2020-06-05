package de.robolab.common.planet

data class PathSelect(
    val point: Coordinate,
    val direction: Direction
) {

    fun translate(delta: Coordinate) = PathSelect(
        point.translate(delta),
        direction
    )

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate) = PathSelect(
        point.rotate(direction, origin),
        this.direction.rotate(direction)
    )
}
