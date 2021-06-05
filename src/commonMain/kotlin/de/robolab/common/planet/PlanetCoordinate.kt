package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable

@Serializable
data class PlanetCoordinate(
    val x: Double,
    val y: Double,
) : IPlanetValue<PlanetCoordinate> {

    val point: Vector
        get() = Vector(x, y)

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(x = p.x, y = p.x)
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(x = p.x, y = p.x)
        }
}

val Vector.planetCoordinate: PlanetCoordinate
    get() = PlanetCoordinate(x, y)
