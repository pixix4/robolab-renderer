package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetPathSelect(
    val x: Long,
    val y: Long,
    val direction: PlanetDirection,
) : IPlanetValue<PlanetPathSelect> {

    constructor(
        point: PlanetPoint,
        direction: PlanetDirection
    ) : this(point.x, point.y, direction)

    constructor(vertex: PlanetPathVertex) : this(vertex.point, vertex.direction)

    val point: PlanetPoint
        get() = PlanetPoint(x, y)

    val vertex: PlanetPathVertex
        get() = PlanetPathVertex(point, direction)

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(x = p.x.roundToLong(), y = p.y.roundToLong())
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(
                direction = this.direction.rotate(direction, origin),
                x = p.x.roundToLong(),
                y = p.y.roundToLong()
            )
        }
}
