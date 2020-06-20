package de.robolab.server.data

import de.robolab.common.planet.ServerPlanetInfo
import kotlin.contracts.Returns
import de.robolab.server.model.ServerPlanet as SPlanet

interface IPlanetStore {
    suspend fun add(planet: SPlanet.Template): SPlanet
    suspend fun remove(planet: SPlanet): Boolean
    suspend fun remove(id: String): SPlanet?
    suspend fun removeBlind(id: String)

    suspend fun update(planet: SPlanet)

    suspend fun get(id: String): SPlanet?
    suspend fun getInfo(id: String): ServerPlanetInfo?

    suspend fun listPlanets(): List<ServerPlanetInfo>
}

suspend fun IPlanetStore.get(info: ServerPlanetInfo?): SPlanet? {
    return get((info ?: return null).id)
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