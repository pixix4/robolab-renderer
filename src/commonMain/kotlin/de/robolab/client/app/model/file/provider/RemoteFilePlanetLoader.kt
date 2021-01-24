package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.planets.*
import de.robolab.common.planet.ID
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    val server: IRobolabServer
) : IFilePlanetLoader {

    override val onRemoteChange = EventHandler<RemoteIdentifier>()

    override val planetCountProperty = constObservable(0)

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(server.hostURL)

    override val iconProperty = constObservable(MaterialIcon.CLOUD)

    override val availableProperty = property(true)
    var available by availableProperty

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, List<String>>? {
        return withContext(Dispatchers.Default) {
            try {
                val result = server.getPlanet(ID(id)).okOrThrow()
                available = true
                RemoteMetadata.Planet(result.planet.name, result.lastModified) to result.lines
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    override suspend fun savePlanet(id: String, lines: List<String>): RemoteIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                server.putPlanet(ID(id), lines.joinToString("\n")).okOrThrow()
                available = true
                RemoteIdentifier(id, loadPlanet(id)?.first ?: return@withContext null)
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    override suspend fun createPlanet(parentId: String, lines: List<String>): RemoteIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                val info = server.postPlanet(lines.joinToString("\n")).okOrThrow().info
                RemoteIdentifier(info.id.toString(), RemoteMetadata.Planet(info.name, info.lastModified))
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    override suspend fun deletePlanet(id: String): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.deletePlanet(ID(id)).okOrThrow()
                true
            } catch (e: Exception) {
                available = false
                false
            }
        }
    }

    override suspend fun listPlanets(id: String): List<RemoteIdentifier>? {
        return withContext(Dispatchers.Default) {
            try {
                available = true

                val value = server.listPlanetDirectory(if (id.isEmpty()) null else id).okOrThrow().decodedValue

                value.subdirectories.map { info ->
                    RemoteIdentifier(
                        info.path,
                        RemoteMetadata.Directory(info.name, info.lastModified, info.childrenCount)
                    )
                } + value.planets.map { info ->
                    RemoteIdentifier(info.id.toString(), RemoteMetadata.Planet(info.name, info.lastModified))
                }
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<RemoteIdentifier>? {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                if (matchExact) {
                    server.listPlanets(nameExact = search).okOrThrow().planets
                } else {
                    server.listPlanets(nameContains = search).okOrThrow().planets
                }.map { info ->
                    RemoteIdentifier(info.id.toString(), RemoteMetadata.Planet(info.name, info.lastModified))
                }
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    companion object {
        fun create(uri: String): RemoteFilePlanetLoader {
            val host = uri.substringAfter("://").trimEnd('/')
            val restRobolabServer = RESTRobolabServer(host, 0, !uri.startsWith("http://"))
            return RemoteFilePlanetLoader(restRobolabServer)
        }
    }
}
