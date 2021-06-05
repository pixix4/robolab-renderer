package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.Serializable

@Serializable
data class PlanetSpline(
    val type: PlanetSplineType,
    val controlPoints: List<PlanetCoordinate>,
) : IPlanetValue<PlanetSpline> {

    fun length(
        sourceX: Long,
        sourceY: Long,
        targetX: Long,
        targetY: Long
    ): Double {
        return type.length(
            sourceX, sourceY, targetX, targetY, controlPoints
        )
    }

    fun reversed() = copy(
        controlPoints = controlPoints.reversed()
    )

    override fun translate(delta: PlanetPoint) = copy(
        controlPoints = controlPoints.translate(delta)
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        controlPoints = controlPoints.rotate(direction, origin)
    )
}
