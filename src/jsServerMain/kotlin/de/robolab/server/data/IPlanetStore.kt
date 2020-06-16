package de.robolab.server.data

import de.robolab.common.planet.ID
import de.robolab.common.planet.PlanetInfo
import de.robolab.server.model.ServerPlanet as SPlanet

interface IPlanetStore {
    suspend fun add(planet: SPlanet): ID?
    suspend fun remove(planet: SPlanet): Boolean
    suspend fun remove(id: ID): SPlanet?

    suspend fun get(id: ID): SPlanet?
    suspend fun get(name: String): List<SPlanet>

    suspend fun listPlanets(): List<PlanetInfo>
}

suspend fun IPlanetStore.listPlanets(name: String, ignoreCase: Boolean) =
    listPlanets().filter { it.name.equals(name, ignoreCase) }

suspend fun IPlanetStore.listPlanets(
    nameStartsWith: String?,
    nameContains: String?,
    nameEndsWith: String?,
    ignoreCase: Boolean
) = listPlanets().filter {
    (nameStartsWith == null || it.name.startsWith(nameStartsWith, ignoreCase)) &&
            (nameContains == null || it.name.contains(nameContains, ignoreCase)) &&
            (nameEndsWith == null || it.name.endsWith(nameEndsWith, ignoreCase))
}