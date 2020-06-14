package de.robolab.common.planet

data class TargetPoint(
    val target: Coordinate,
    val exposure: Coordinate
): IPlanetValue {

    fun translate(delta: Coordinate) = TargetPoint(
        target.translate(delta),
        exposure.translate(delta)
    )

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate) = TargetPoint(
        target.rotate(direction, origin),
        exposure.rotate(direction, origin)
    )
}
