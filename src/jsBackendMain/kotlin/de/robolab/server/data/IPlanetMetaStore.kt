package de.robolab.server.data

import de.robolab.common.planet.utils.ServerPlanetInfo

interface IPlanetMetaStore {
    suspend fun retrieveFilePath(id: String, verifier: suspend (String) -> Boolean): String?
    suspend fun retrieveFilePath(
        id: String,
        verifier: suspend (String) -> Boolean,
        lookup: suspend (String) -> String?
    ): String? {
        val path = retrieveFilePath(id, verifier)
        if (path != null) return path
        val newPath = lookup(id) ?: return null
        setFilePath(id, newPath)
        return newPath
    }

    suspend fun retrieveFilePath(ids: List<String>, verifier: suspend (String) -> Boolean): List<String?> =
        ids.map { retrieveFilePath(it, verifier) }

    suspend fun retrieveFilePath(
        ids: List<String>,
        verifier: suspend (String) -> Boolean,
        lookup: suspend (String) -> String?
    ): List<String?> {
        suspend fun batchLookup(innerIDs: List<String>): List<String?> {
            val result = mutableListOf<String?>()
            for (id in innerIDs) //map does not seem to work with suspend?
                result.add(lookup(id))
            return result
        }
        return retrieveFilePath(ids, verifier, ::batchLookup)
    }

    suspend fun retrieveFilePath(
        ids: List<String>,
        verifier: suspend (String) -> Boolean,
        lookup: suspend (List<String>) -> List<String?>
    ): List<String?> {
        val cachedPath: MutableList<String?> = retrieveFilePath(ids, verifier).toMutableList()
        if (!cachedPath.contains(null)) return cachedPath.filterNotNull()
        val missingIndices: List<Int> = cachedPath.withIndex().filter { it.value == null }.map { it.index }
        val missingIDs: List<String> = missingIndices.map(ids::get)
        val missingPaths: List<String?> = lookup(missingIDs)
        for ((pathIndex, index) in missingIndices.withIndex()) {
            val missingPath: String = missingPaths[pathIndex] ?: continue
            cachedPath[index] = missingPath
        }
        setFilePath(missingIDs.zip(missingPaths))
        return cachedPath
    }

    suspend fun retrieveInfo(id: String): ServerPlanetInfo?
    suspend fun retrieveInfo(ids: List<String>): List<ServerPlanetInfo?> = ids.map { retrieveInfo(it) }
    suspend fun retrieveInfo(id: String, lookup: suspend (String) -> ServerPlanetInfo?): ServerPlanetInfo? {
        val cachedInfo: ServerPlanetInfo? = retrieveInfo(id)
        if (cachedInfo != null) return cachedInfo
        val newInfo = lookup(id)
        if (newInfo != null)
            setInfo(newInfo)
        return newInfo
    }

    suspend fun retrieveInfo(
        ids: List<String>,
        lookup: suspend (String) -> ServerPlanetInfo?
    ): List<ServerPlanetInfo?> {
        val cachedInfo: List<ServerPlanetInfo?> = retrieveInfo(ids)
        if (!cachedInfo.contains(null)) return cachedInfo.filterNotNull()
        val info: List<Pair<String?, ServerPlanetInfo?>> = //key==null: no update needed; value==null: Exception
            cachedInfo.zip(ids) { info, id ->
                if (info != null) null to info
                else id to try {
                    lookup(id)
                } catch (ex: Exception) {
                    console.error(ex)
                    null
                }
            }
        val infoUpdate: List<ServerPlanetInfo> =
            info.filter { it.first != null && it.second != null }.map { it.second!! }
        setInfo(infoUpdate)
        return info.map(Pair<String?, ServerPlanetInfo?>::second)
    }

    suspend fun setFilePath(id: String, path: String?)
    suspend fun setFilePath(idPathPairs: List<Pair<String, String?>>) {
        idPathPairs.forEach { setFilePath(it.first, it.second) }
    }

    suspend fun setInfo(info: ServerPlanetInfo, onlyIfExist: Boolean = false)
    suspend fun setInfo(infos: List<ServerPlanetInfo>, onlyIfExist: Boolean = false) =
        infos.forEach { setInfo(it, onlyIfExist) }

    suspend fun addInfo(info: ServerPlanetInfo)
    suspend fun addInfo(infos: List<ServerPlanetInfo>) = infos.forEach { addInfo(it) }
    suspend fun removeInfoByID(id: String): ServerPlanetInfo?
    suspend fun removeInfoByID(ids: List<String>): List<ServerPlanetInfo?> = ids.map { removeInfoByID(it) }

    suspend fun clear(): Pair<Boolean, String>
}

suspend fun IPlanetMetaStore.setInfo(vararg info: ServerPlanetInfo, onlyIfExist: Boolean = false) =
    setInfo(info.asList(), onlyIfExist)

suspend fun IPlanetMetaStore.addInfo(vararg info: ServerPlanetInfo) = addInfo(info.asList())