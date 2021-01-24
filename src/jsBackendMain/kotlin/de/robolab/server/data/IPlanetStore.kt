package de.robolab.server.data

import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.net.data.DirectoryInfo
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.model.ServerPlanet as SPlanet

interface IPlanetStore {
    suspend fun add(planet: SPlanet.Template, path: String? = null): SPlanet
    suspend fun remove(planet: SPlanet): Boolean
    suspend fun remove(id: String): SPlanet?
    suspend fun removeBlind(id: String)

    suspend fun update(planet: SPlanet)

    suspend fun get(id: String): SPlanet?
    suspend fun getInfo(id: String): ServerPlanetInfo?

    suspend fun listPlanets(path: String): List<ServerPlanetInfo>
    suspend fun listLivePlanets(path: String): List<ServerPlanetInfo>
    suspend fun listFileEntries(path: String): DirectoryInfo.ServerContentInfo?

    suspend fun clearMeta(): Pair<Boolean, String>
    suspend fun isPlanetPath(path: String): Boolean
    suspend fun internalPlanetIDFromPath(path: String): String
}

suspend fun IPlanetStore.get(info: ServerPlanetInfo?): SPlanet? {
    return get((info ?: return null).id)
}

suspend fun IPlanetStore.listPlanets(path: String, name: String, ignoreCase: Boolean) =
    listPlanets(path).filter { it.name.equals(name, ignoreCase) }

suspend fun IPlanetStore.listPlanets(
    path: String,
    nameStartsWith: String?,
    nameContains: String?,
    nameEndsWith: String?,
    ignoreCase: Boolean
) = listPlanets(path).filter {
    (nameStartsWith == null || it.name.startsWith(nameStartsWith, ignoreCase)) &&
            (nameContains == null || it.name.contains(nameContains, ignoreCase)) &&
            (nameEndsWith == null || it.name.endsWith(nameEndsWith, ignoreCase))
}

suspend fun IPlanetStore.listPlanets(path: String, query: SearchRequest, ignoreCase: Boolean) =
    listPlanets(path).filter { query.matches(it, ignoreCase) }

suspend fun IPlanetStore.listLivePlanets(path: String, name: String, ignoreCase: Boolean) =
    listLivePlanets(path).filter { it.name.equals(name, ignoreCase) }

suspend fun IPlanetStore.listLivePlanets(
    path: String,
    nameStartsWith: String?,
    nameContains: String?,
    nameEndsWith: String?,
    ignoreCase: Boolean
) = listLivePlanets(path).filter {
    (nameStartsWith == null || it.name.startsWith(nameStartsWith, ignoreCase)) &&
            (nameContains == null || it.name.contains(nameContains, ignoreCase)) &&
            (nameEndsWith == null || it.name.endsWith(nameEndsWith, ignoreCase))
}

suspend fun IPlanetStore.listLivePlanets(path: String, query: SearchRequest, ignoreCase: Boolean) =
    listLivePlanets(path).filter { query.matches(it, ignoreCase) }

suspend fun IPlanetStore.listFileEntries(
    path: String,
    name: String,
    ignoreCase: Boolean
): DirectoryInfo.ServerContentInfo? {
    val info = listFileEntries(path);
    return info?.copy(
        subdirectories = info.subdirectories.filter { it.name.equals(name, ignoreCase) },
        planets = info.planets.filter { it.name.equals(name, ignoreCase) })
}

suspend fun IPlanetStore.listFileEntries(
    path: String,
    nameStartsWith: String?,
    nameContains: String?,
    nameEndsWith: String?,
    ignoreCase: Boolean
): DirectoryInfo.ServerContentInfo? {
    val info = listFileEntries(path)
    return info?.copy(subdirectories = info.subdirectories.filter {
        (nameStartsWith == null || it.name.startsWith(nameStartsWith, ignoreCase)) &&
                (nameContains == null || it.name.contains(nameContains, ignoreCase)) &&
                (nameEndsWith == null || it.name.endsWith(nameEndsWith, ignoreCase))
    }, planets = info.planets.filter {
        (nameStartsWith == null || it.name.startsWith(nameStartsWith, ignoreCase)) &&
                (nameContains == null || it.name.contains(nameContains, ignoreCase)) &&
                (nameEndsWith == null || it.name.endsWith(nameEndsWith, ignoreCase))
    })
}