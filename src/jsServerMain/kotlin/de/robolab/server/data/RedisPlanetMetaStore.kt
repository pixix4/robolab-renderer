package de.robolab.server.data

import de.robolab.common.planet.ID
import de.robolab.common.planet.PlanetInfo
import de.robolab.common.utils.Logger
import de.robolab.server.externaljs.ioredis.Redis
import de.robolab.server.externaljs.ioredis.createRedis
import de.robolab.server.externaljs.ioredis.mget
import de.robolab.server.externaljs.ioredis.msetnx
import de.robolab.server.externaljs.toList
import de.robolab.server.model.toID
import kotlinx.coroutines.await

class RedisPlanetMetaStore(connectionString: String) : IPlanetMetaStore {

    private val redis: Redis = createRedis(connectionString)

    override suspend fun retrieveInfo(ids: List<String>, lookup: suspend (String) -> PlanetInfo): List<PlanetInfo> {
        val cachedNames = redis.mget(ids.map { "planet:name@$it" }).await().toList()
        if (!cachedNames.contains(null)) return cachedNames.zip(ids) { name, id -> PlanetInfo(id.toID(), name!!) }
        val info: List<Pair<String?, PlanetInfo?>> = //key==null: no update needed
            cachedNames.zip(ids) { s, id ->
                if (s != null) null to PlanetInfo(id.toID(), s)
                else id to try {
                    lookup(id)
                } catch (ex: Exception) {
                    console.error(ex)
                    null
                }
            }
        val infoUpdate: List<Pair<String, PlanetInfo>> =
            info.filter { it.first != null && it.second != null }.map { it.first!! to it.second!! }
        redis.msetnx(infoUpdate.map { "planet:name@${it.first}" to it.second.name })
        return info.map(Pair<String?, PlanetInfo?>::second).filterNotNull()
    }

    override suspend fun retrieveInfo(ids: List<String>): List<PlanetInfo?> =
        redis.mget(ids.map { "planet:name@$it" }).await().toList()
            .zip(ids) { name, id -> if (name != null) PlanetInfo(ID(id), name) else null }

    protected fun finalize() {
        redis.disconnect()
    }
}