package de.robolab.server.data

import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.externaljs.ioredis.*
import de.robolab.server.externaljs.toList
import kotlinx.coroutines.await

class RedisPlanetMetaStore(connectionString: String) : IPlanetMetaStore {

    private val redis: Redis = createRedis(connectionString)

    override suspend fun retrieveInfo(id: String): ServerPlanetInfo? =
        redis.get("planet:name@$id").await().let { if (it != null) ServerPlanetInfo(id, it) else null }

    override suspend fun retrieveInfo(ids: List<String>): List<ServerPlanetInfo?> =
        redis.mget(ids.map { "planet:name@$it" }).await().toList()
            .zip(ids) { name, id -> if (name != null) ServerPlanetInfo(id, name) else null }

    override suspend fun setInfo(info: ServerPlanetInfo, onlyIfExist: Boolean) {
        if (onlyIfExist)
            redis.setxx("planet:name@${info.id}", info.name).await()
        else
            redis.set("planet:name@${info.id}", info.name).await()
    }

    override suspend fun addInfo(info: ServerPlanetInfo) {
        redis.setnx("planet:name@${info.id}", info.name).await()
    }

    override suspend fun removeInfoByID(id: String): ServerPlanetInfo? {
        val storedName = redis.transaction {
            get("planet:name@$id")
            del("planet:name@$id")
        }[0] as String?
        return if (storedName == null || storedName == undefined)
            null
        else
            ServerPlanetInfo(id, storedName)
    }

    override suspend fun setInfo(infos: List<ServerPlanetInfo>, onlyIfExist: Boolean) {
        if (onlyIfExist)
            super.setInfo(infos)
        else
            redis.mset(infos.map { "planet:name@${it.id}" to it.name }).await()
    }

    override suspend fun addInfo(infos: List<ServerPlanetInfo>) {
        redis.msetnx(infos.map { "planet:name@${it.id}" to it.name }).await()
    }


    protected fun finalize() {
        redis.disconnect()
    }
}