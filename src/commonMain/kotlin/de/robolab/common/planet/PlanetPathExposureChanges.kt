package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import kotlinx.serialization.Serializable

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

fun PlanetPathExposureChanges?.applyUpdateOrPass(path: PlanetPath): PlanetPath =
    this?.applyUpdate(path) ?: path
