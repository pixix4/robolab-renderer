package de.robolab.server.data

import de.robolab.common.planet.PlanetInfo

interface IPlanetMetaStore{
    suspend fun retrieveInfo(ids: List<String>): List<PlanetInfo?>
    suspend fun retrieveInfo(ids: List<String>, lookup: suspend (String)->PlanetInfo): List<PlanetInfo>
}