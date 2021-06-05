package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetTarget(
    val x: Long,
    val y: Long,
    val exposure: Set<PlanetPoint>,
) : IPlanetValue<PlanetTarget> {

    constructor(
        point: PlanetPoint,
        exposure: Set<PlanetPoint>,
    ) : this(point.x, point.y, exposure)

    val point: PlanetPoint
        get() = PlanetPoint(x, y)

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(
            exposure = exposure.translate(delta),
            x = p.x.roundToLong(),
            y = p.x.roundToLong()
        )
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(
                exposure = exposure.rotate(direction, origin),
                x = p.x.roundToLong(),
                y = p.x.roundToLong()
            )
        }
}
