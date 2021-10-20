package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetStartPoint(
    val x: Long,
    val y: Long,
    val orientation: PlanetDirection,
    val spline: PlanetSpline? = null,
) : IPlanetValue<PlanetStartPoint> {

    constructor(
        point: PlanetPoint,
        orientation: PlanetDirection,
        spline: PlanetSpline? = null
    ): this(point.x, point.y, orientation, spline)

    val point: PlanetPoint
        get() = PlanetPoint(x, y)

    val path = PlanetPath(
        sourceX = x,
        sourceY = y,
        sourceDirection = orientation.opposite(),
        targetX = x,
        targetY = y,
        targetDirection = orientation.opposite(),
        weight = 0L,
        exposure = emptySet(),
        hidden = false,
        spline = spline,
        arrow = true,
    )

    val vertex: PlanetPathVertex
        get() = PlanetPathVertex(point,  orientation.opposite())

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(
            spline = spline.translate(delta),
            x = p.x.roundToLong(),
            y = p.y.roundToLong()
        )
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(
                orientation = orientation.rotate(direction, origin),
                spline = spline.rotate(direction, origin),
                x = p.x.roundToLong(),
                y = p.y.roundToLong()
            )
        }
}
