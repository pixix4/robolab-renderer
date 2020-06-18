package de.robolab.common.planet

data class PlanetInfo<IDType>(
    val id: IDType,
    val name: String
)

typealias ClientPlanetInfo = PlanetInfo<ID>
typealias ServerPlanetInfo = PlanetInfo<String>