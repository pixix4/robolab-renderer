package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.*
import de.robolab.client.net.requests.planets.*
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    val server: IRobolabServer
) : IFilePlanetLoader<PlanetJsonInfo> {

    private val logger = Logger(this)

    override val onRemoteChange = EventHandler<PlanetJsonInfo?>()

    override val planetCountProperty = constObservable(0)

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(server.hostURL)

    override val iconProperty = constObservable(MaterialIcon.CLOUD_QUEUE)

    override val availableProperty = property(true)
    var available by availableProperty

    override suspend fun loadPlanet(identifier: PlanetJsonInfo): Pair<PlanetJsonInfo, List<String>>? {
        return withContext(Dispatchers.Default) {
            try {
                val result = server.getPlanet(identifier.id).okOrThrow()
                available = true
                PlanetJsonInfo(identifier.id, identifier.name, result.lastModified, identifier.tags) to result.lines
            } catch (e: Exception) {
                logger.error("loadPlanet", e)
                available = false
                null
            }
        }
    }

    override suspend fun savePlanet(identifier: PlanetJsonInfo, lines: List<String>): PlanetJsonInfo? {
        return withContext(Dispatchers.Default) {
            try {
                server.putPlanet(identifier.id, lines.joinToString("\n")).okOrThrow()
                available = true
                identifier
            } catch (e: Exception) {
                logger.error("savePlanet", e)
                available = false
                null
            }
        }
    }

    override suspend fun createPlanet(identifier: PlanetJsonInfo?, lines: List<String>) {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.postPlanet(lines.joinToString("\n")).okOrThrow()
            } catch (e: Exception) {
                logger.error("createPlanet", e)
                available = false
            }
        }
    }

    override suspend fun deletePlanet(identifier: PlanetJsonInfo) {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.deletePlanet(identifier.id).okOrThrow()
            } catch (e: Exception) {
                logger.error("deletePlanet", e)
                available = false
            }
        }
    }

    override suspend fun listPlanets(identifier: PlanetJsonInfo?): List<PlanetJsonInfo> {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.listPlanets().okOrThrow().planets
            } catch (e: Exception) {
                logger.error("listPlanets", e)
                available = false
                emptyList()
            }
        }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<PlanetJsonInfo> {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                if (matchExact) {
                    server.listPlanets(nameExact = search).okOrThrow().planets
                } else  {
                    server.listPlanets(nameContains = search).okOrThrow().planets
                }
            } catch (e: Exception) {
                logger.error("searchPlanets", e)
                available = false
                emptyList()
            }
        }
    }

    object HttpFactory : IFilePlanetLoaderFactory {

        override val protocol = "http"

        override val usage: String = "$protocol://example.org"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val host = HttpsFactory.parse(uri)
            val restRobolabServer = RESTRobolabServer(host, 0, false)
            return RemoteFilePlanetLoader(restRobolabServer)
        }
    }

    object HttpsFactory : IFilePlanetLoaderFactory {

        override val protocol = "https"

        override val usage: String = "$protocol://example.org"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val host = parse(uri)
            val restRobolabServer = RESTRobolabServer(host, 0, true)
            return RemoteFilePlanetLoader(restRobolabServer)
        }

        fun parse(uri: String): String {
            return uri.substringAfter("://").trimEnd('/')
        }
    }
}
