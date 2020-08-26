package de.robolab.server.data

import de.robolab.common.planet.ServerPlanetInfo

interface IPlanetMetaStore {
    suspend fun retrieveInfo(id: String): ServerPlanetInfo?
    suspend fun retrieveInfo(ids: List<String>): List<ServerPlanetInfo?> = ids.map { retrieveInfo(it) }
    suspend fun retrieveInfo(id: String, lookup: suspend (String) -> ServerPlanetInfo?): ServerPlanetInfo? {
        val cachedInfo: ServerPlanetInfo? = retrieveInfo(id)
        if (cachedInfo != null) return cachedInfo
        val newInfo = lookup(id)
        if(newInfo != null)
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

    suspend fun setInfo(info: ServerPlanetInfo, onlyIfExist: Boolean = false)
    suspend fun setInfo(infos: List<ServerPlanetInfo>, onlyIfExist: Boolean = false) =
        infos.forEach { setInfo(it, onlyIfExist) }

    suspend fun addInfo(info: ServerPlanetInfo)
    suspend fun addInfo(infos: List<ServerPlanetInfo>) = infos.forEach { addInfo(it) }
    suspend fun removeInfoByID(id: String): ServerPlanetInfo?
    suspend fun removeInfoByID(ids: List<String>): List<ServerPlanetInfo?> = ids.map { removeInfoByID(it) }

    suspend fun clear(): Pair<Boolean,String>
}

suspend fun IPlanetMetaStore.setInfo(vararg info: ServerPlanetInfo, onlyIfExist: Boolean = false) =
    setInfo(info.asList(), onlyIfExist)

suspend fun IPlanetMetaStore.addInfo(vararg info: ServerPlanetInfo) = addInfo(info.asList())