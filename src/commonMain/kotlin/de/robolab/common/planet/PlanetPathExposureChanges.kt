package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetPathExposureChanges(
    val weight: Long
) : IPlanetValue<PlanetPathExposureChanges> {

    fun applyUpdate(path: PlanetPath): PlanetPath {
        return path.copy(
            weight = weight
        )
    }
}
