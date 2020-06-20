package de.robolab.server.data

import com.soywiz.klock.DateTime
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.externaljs.ioredis.*
import de.robolab.server.externaljs.toList
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope

class RedisPlanetMetaStore(connectionString: String) : IPlanetMetaStore {

    private val redis: Redis = createRedis(connectionString)

    override suspend fun retrieveInfo(id: String): ServerPlanetInfo? =
        redis.mget("planet:name@$id", "planet:mtime@$id").await().let {
            val (name, mtime) = it.toList()
            if (name != null && mtime != null) ServerPlanetInfo(
                id,
                name,
                DateTime.Companion.fromUnix(mtime.toLong())
            ) else null
        }

    override suspend fun retrieveInfo(ids: List<String>): List<ServerPlanetInfo?> =
        redis.mget(ids.flatMap {
            listOf(
                "planet:name@$it",
                "planet:mtime@$it"
            )
        }).await().toList().chunked(2)
            .zip(ids) { (name, mtime), id ->
                if (name != null && mtime != null) ServerPlanetInfo(
                    id,
                    name,
                    DateTime.fromUnix(mtime.toLong())
                ) else null
            }

    override suspend fun setInfo(info: ServerPlanetInfo, onlyIfExist: Boolean) {
        if (onlyIfExist) {
            redis.setxx("planet:name@${info.id}", info.name).await()
            redis.setxx("planet:mtime@${info.id}", info.lastModified.unixMillisLong.toString())
        } else{
            redis.set("planet:name@${info.id}", info.name).await()
            redis.set("planet:mtime@${info.id}", info.lastModified.unixMillisLong.toString())
        }
    }

    override suspend fun addInfo(info: ServerPlanetInfo) {
        redis.setnx("planet:name@${info.id}", info.name).await()
        redis.setnx("planet:mtime@${info.id}", info.lastModified.unixMillisLong.toString())
    }

    override suspend fun removeInfoByID(id: String): ServerPlanetInfo? {

        /*val (storedName: String?, storedMtime: String?) = redis.transaction {
            get("planet:name@$id")
            get("planet:mtime@$id")
            del("planet:name@$id")
            del("planet:mtime@$id")
        }.toList().subList(0, 2).map { it as String? }*/
        val storedName = redis.get("planet:name@$id").await()
        val storedMtime = redis.get("planet:mtime@$id").await()
        redis.del("planet:name@$id")
        redis.del("planet:mtime@$id")
        return if (storedName == null || storedName == undefined || storedMtime == null || storedMtime == undefined)
            null
        else
            ServerPlanetInfo(id, storedName, DateTime.Companion.fromUnix(storedMtime.toLong()))
    }

    override suspend fun setInfo(infos: List<ServerPlanetInfo>, onlyIfExist: Boolean) {
        if (onlyIfExist)
            super.setInfo(infos)
        else
            redis.mset(infos.flatMap {
                listOf(
                    "planet:name@${it.id}" to it.name,
                    "planet:mtime@${it.id}" to it.lastModified.unixMillisLong.toString()
                )
            }).await()
    }

    override suspend fun addInfo(infos: List<ServerPlanetInfo>) {
        redis.msetnx(infos.flatMap {
            listOf(
                "planet:name@${it.id}" to it.name,
                "planet:mtime@${it.id}" to it.lastModified.unixMillisLong.toString()
            )
        }).await()
    }


    protected fun finalize() {
        redis.disconnect()
    }
}