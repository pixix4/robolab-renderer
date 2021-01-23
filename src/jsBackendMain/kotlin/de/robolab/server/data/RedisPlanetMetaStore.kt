package de.robolab.server.data

import com.soywiz.klock.DateTime
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.externaljs.dynamicOfDefined
import de.robolab.server.externaljs.ioredis.*
import de.robolab.common.externaljs.toList
import de.robolab.common.jsutils.jsTruthy
import de.robolab.common.utils.Logger
import kotlinx.coroutines.await

class RedisPlanetMetaStore(connectionString: String, connectionName: String = "") : IPlanetMetaStore {

    private companion object {
        private fun planetNameKey(id: String) = "planet:name@$id"
        private fun planetMTimeKey(id: String) = "planet:mtime@$id"
        private fun planetTagsKey(id: String) = "planet:tags@$id"
        private fun planetTagKey(id: String, tag: String) = "planet:tag:$tag@$id"
        private fun planetPathKey(id: String) = "planet:file@$id"

        private const val TIMEOUT_THROTTLER = 30_000L
    }

    private val redis: Redis = createRedis(
        connectionString,
        options = dynamicOfDefined(
            "connectionName" to if (connectionName.isEmpty()) undefined else connectionName
        )
    )

    private suspend fun setTag(id: String, tagName: String, tagValues: List<String>) {
        redis.del(planetTagKey(id, tagName)).await()
        if (tagValues.isNotEmpty())
            redis.lpush(planetTagKey(id, tagName), tagValues[0], *tagValues.drop(1).toTypedArray())
        redis.sadd(planetTagsKey(id), tagName).await()
    }

    private suspend fun removeTag(id: String, tagName: String) {
        redis.srem(planetTagsKey(id), tagName).await()
        redis.del(planetTagKey(id, tagName)).await()
    }

    private suspend fun removeTags(id: String) {
        val oldTags: List<String> = redis.smembers(planetTagsKey(id)).await().toList().filterNotNull()
        redis.del(planetTagsKey(id), *oldTags.map { planetTagKey(id, it) }.toTypedArray()).await()
    }

    private suspend fun setTag(id: String, tagName: String, tagValues: List<String>?) =
        if (tagValues == null) removeTag(id, tagName) else
            setTag(id, tagName, tagValues)


    private suspend fun setTags(id: String, tags: Map<String, List<String>>) {
        removeTags(id)
        tags.forEach { (tagName, values) -> setTag(id, tagName, values) }
    }

    private suspend fun setTags(data: Map<String, Map<String, List<String>>>) =
        data.forEach { (id, tags) -> setTags(id, tags) }


    private suspend fun getTags(id: String): Map<String, List<String>> {
        return redis.smembers(planetTagsKey(id)).await().toList().filterNotNull().associateWith { tag ->
            redis.lrange(planetTagKey(id, tag), 0, -1).await().toList().mapNotNull {
                it as? String
            }
        }
    }

    private suspend fun getTags(ids: List<String>): Map<String, Map<String, List<String>>> =
        ids.associateWith { getTags(it) }

    override suspend fun retrieveFilePath(id: String, verifier: suspend (String) -> Boolean): String? {
        val response = redis.get(planetPathKey(id)).await()
        if (response == null || response == undefined || !verifier(response)) return null
        return response
    }

    override suspend fun setFilePath(id: String, path: String?) {
        if (path == null) {
            redis.del(planetPathKey(id))
            return
        }
        redis.set(planetPathKey(id), path)
    }

    override suspend fun retrieveInfo(id: String): ServerPlanetInfo? =
        redis.mget(planetNameKey(id), planetMTimeKey(id)).await().let { data ->
            val (name, mtime) = data.toList()
            val tags = getTags(id)
            if (name != null && mtime != null) ServerPlanetInfo(
                id,
                name,
                DateTime.Companion.fromUnix(mtime.toLong()),
                tags
            ) else null
        }

    override suspend fun retrieveInfo(ids: List<String>): List<ServerPlanetInfo?> {

        val tags: Map<String, Map<String, List<String>>> = getTags(ids)
        return redis.mget(ids.flatMap {
            listOf(
                planetNameKey(it),
                planetMTimeKey(it)
            )
        }).await().toList().chunked(2).zip(ids) { (name, mtime), id ->
            if (name != null && mtime != null) ServerPlanetInfo(
                id,
                name,
                DateTime.fromUnix(mtime.toLong()),
                tags = tags[id].orEmpty()
            ) else null
        }.toList()
    }

    override suspend fun setInfo(info: ServerPlanetInfo, onlyIfExist: Boolean) {
        if (onlyIfExist) {
            redis.setxx(planetNameKey(info.id), info.name).await()
            if (redis.setxx(planetMTimeKey(info.id), info.lastModified.unixMillisLong.toString()).await().jsTruthy())
                setTags(info.id, info.tags)
        } else {
            redis.set(planetNameKey(info.id), info.name).await()
            redis.set(planetMTimeKey(info.id), info.lastModified.unixMillisLong.toString()).await()
            setTags(info.id, info.tags)
        }
    }

    override suspend fun addInfo(info: ServerPlanetInfo) {
        redis.setnx(planetNameKey(info.id), info.name).await()
        if (redis.setnx(planetMTimeKey(info.id), info.lastModified.unixMillisLong.toString()).await() > 0)
            setTags(info.id, info.tags)
    }

    override suspend fun removeInfoByID(id: String): ServerPlanetInfo? {

        /*val (storedName: String?, storedMtime: String?) = redis.transaction {
            get("planet:name@$id")
            get("planet:mtime@$id")
            del("planet:name@$id")
            del("planet:mtime@$id")
        }.toList().subList(0, 2).map { it as String? }*/
        val storedName = redis.get(planetNameKey(id)).await()
        val storedMtime = redis.get(planetMTimeKey(id)).await()
        redis.del(planetNameKey(id))
        redis.del(planetMTimeKey(id))
        removeTags(id)
        return if (storedName == null || storedName == undefined || storedMtime == null || storedMtime == undefined)
            null
        else
            ServerPlanetInfo(id, storedName, DateTime.Companion.fromUnix(storedMtime.toLong()))
    }

    override suspend fun setInfo(infos: List<ServerPlanetInfo>, onlyIfExist: Boolean) {
        if (onlyIfExist)
            super.setInfo(infos)
        else {
            infos.forEach { info ->
                setTags(info.id, info.tags)
            }
            redis.mset(infos.flatMap {
                listOf(
                    planetNameKey(it.id) to it.name,
                    planetMTimeKey(it.id) to it.lastModified.unixMillisLong.toString()
                )
            }).await()
        }
    }

    override suspend fun addInfo(infos: List<ServerPlanetInfo>) {
        infos.forEach { setTags(it.id, it.tags) }
        redis.msetnx(infos.flatMap {
            listOf(
                planetNameKey(it.id) to it.name,
                planetMTimeKey(it.id) to it.lastModified.unixMillisLong.toString()
            )
        }).await()
    }

    override suspend fun clear(): Pair<Boolean, String> {
        val text = redis.flushdb().await()
        return ("ok".equals(text, true) to text)
    }

    protected fun finalize() {
        redis.disconnect()
    }

    private val logger = Logger("RedisPlanetMetaStore")

    init {
        var lastReconnectError = 0L
        redis.on("error") { event ->
            if (event.errno == "ECONNREFUSED") {
                val time = DateTime.nowUnixLong()


                if (time - lastReconnectError > TIMEOUT_THROTTLER) {
                    logger.error {
                        "Cannot connect to redis server at '${event.address}:${event.port}'!"
                    }
                    if (lastReconnectError == 0L && Logger.level.index < Logger.Level.DEBUG.index) {
                        logger.warn { "This error message is throttled and appears at a maximum interval of ${TIMEOUT_THROTTLER / 1000}s. To see all messages enable debug logging." }
                    }
                    lastReconnectError = time
                } else {
                    logger.debug {
                        "Cannot connect to redis server at '${event.address}:${event.port}'!"
                    }
                }
            } else {
                logger.error { event }
            }
        }
    }
}
