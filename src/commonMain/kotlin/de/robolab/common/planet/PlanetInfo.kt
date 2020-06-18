package de.robolab.common.planet

import com.soywiz.klock.DateTime

data class PlanetInfo<IDType>(
    val id: IDType,
    val name: String,
    val lastModifiedAt: DateTime
) {
    fun withMTime(time: DateTime): PlanetInfo<IDType> {
        return copy(lastModifiedAt = time)
    }
}

typealias ClientPlanetInfo = PlanetInfo<ID>
typealias ServerPlanetInfo = PlanetInfo<String>