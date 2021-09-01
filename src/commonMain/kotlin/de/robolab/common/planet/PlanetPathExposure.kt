package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetPathExposure(
    val x: Long,
    val y: Long,
    val changes: PlanetPathExposureChanges? = null
) : IPlanetValue<PlanetPathExposure> {

    constructor(
        exposure: PlanetPoint,
        changes: PlanetPathExposureChanges? = null
    ): this(
        exposure.x,
        exposure.y,
        changes
    )

    val isExposure = changes == null
    val isUpdate = changes != null

    val planetPoint: PlanetPoint
        get() = PlanetPoint(x, y)

    val point: Vector
        get() = Vector(x, y)

    fun applyUpdate(path: PlanetPath): PlanetPath {
        return changes?.applyUpdate(path) ?: path
    }

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(
            x = p.x.roundToLong(),
            y = p.y.roundToLong(),
            changes = changes?.translate(delta)
        )
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(
                x = p.x.roundToLong(),
                y = p.y.roundToLong(),
                changes = changes?.rotate(direction, origin)
            )
        }
}
