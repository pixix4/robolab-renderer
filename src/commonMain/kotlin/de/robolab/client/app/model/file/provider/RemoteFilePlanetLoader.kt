package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.PingRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.planets.*
import de.robolab.common.planet.ID
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    s: IRobolabServer
) : IFilePlanetLoader {

    val server = PingRobolabServer(s)

    override val onRemoteChange = EventHandler<RemoteIdentifier>()

    override val planetCountProperty = constObservable(0)

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(server.hostURL)

    override val availableProperty = server.availableProperty

    override val iconProperty = availableProperty.mapBinding { if (it) MaterialIcon.CLOUD else MaterialIcon.CLOUD_OFF }

    override val supportedRemoteModes: List<RemoteMode> = listOf(RemoteMode.NESTED, RemoteMode.FLAT, RemoteMode.LIVE)
    override val remoteModeProperty: ObservableProperty<RemoteMode> = property(RemoteMode.NESTED)

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, List<String>>? {
        return withContext(Dispatchers.Default) {
            try {
                val result = server.getPlanet(ID(id)).okOrThrow()
                RemoteMetadata.Planet(result.planet.name, result.lastModified) to result.lines
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun savePlanet(id: String, lines: List<String>): RemoteIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                server.putPlanet(ID(id), lines.joinToString("\n")).okOrThrow()
                RemoteIdentifier(id, loadPlanet(id)?.first ?: return@withContext null)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun createPlanet(parentId: String, lines: List<String>): RemoteIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                val info = server.postPlanet(lines.joinToString("\n")).okOrThrow().info
                RemoteIdentifier(info.id.toString(), RemoteMetadata.Planet(info.name, info.lastModified))
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun deletePlanet(id: String): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                server.deletePlanet(ID(id)).okOrThrow()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun listPlanets(id: String): List<RemoteIdentifier>? {
        return withContext(Dispatchers.Default) {
            try {
                when (remoteModeProperty.value) {
                    RemoteMode.NESTED -> {
                        val value = server.listPlanetDirectory(if (id.isEmpty()) null else id).okOrThrow().decodedValue

                        value.subdirectories.map { info ->
                            RemoteIdentifier(
                                info.path,
                                RemoteMetadata.Directory(info.name, info.lastModified, info.childrenCount)
                            )
                        } + value.planets.map { info ->
                            RemoteIdentifier(
                                info.id.toString(),
                                RemoteMetadata.Planet(info.name, info.lastModified, info.tags)
                            )
                        }
                    }
                    RemoteMode.FLAT -> server.listPlanets(liveOnly = false).okOrThrow().planets.map { info ->
                        RemoteIdentifier(
                            info.id.toString(),
                            RemoteMetadata.Planet(info.name, info.lastModified, info.tags)
                        )
                    }
                    RemoteMode.LIVE -> server.listPlanets(liveOnly = true).okOrThrow().planets.map { info ->
                        RemoteIdentifier(
                            info.id.toString(),
                            RemoteMetadata.Planet(info.name, info.lastModified, info.tags)
                        )
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<RemoteIdentifier>? {
        return withContext(Dispatchers.Default) {
            try {
                if (matchExact) {
                    server.listPlanets(nameExact = search).okOrThrow().planets
                } else {
                    server.listPlanets(nameContains = search).okOrThrow().planets
                }.map { info ->
                    RemoteIdentifier(info.id.toString(), RemoteMetadata.Planet(info.name, info.lastModified))
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    companion object {
        private var lastLoader: RemoteFilePlanetLoader? = null
        fun create(uri: String): RemoteFilePlanetLoader {
            val host = uri.substringAfter("://").trimEnd('/')
            lastLoader?.server?.stopPing()
            val restRobolabServer = RESTRobolabServer(host, 0, !uri.startsWith("http://"))
            val loader = RemoteFilePlanetLoader(restRobolabServer)
            loader.server.startPing()
            lastLoader = loader
            return loader
        }
    }
}
