package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.PingRobolabServer
import de.robolab.client.net.requests.planets.*
import de.robolab.common.planet.Planet
import de.robolab.common.planet.utils.ID
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    val server: PingRobolabServer
) : IFilePlanetLoader {

    val logger = Logger(this)

    override val id: String = "remote-loader-${server.hostURL}"

    override val onRemoteChange = EventHandler<RemoteIdentifier>()

    override val planetCountProperty = constObservable(0)

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(server.hostURL)

    override val availableProperty = server.availableProperty

    override val iconProperty = availableProperty.mapBinding { if (it) MaterialIcon.CLOUD else MaterialIcon.CLOUD_OFF }

    override val supportedRemoteModes: List<RemoteMode> = listOf(RemoteMode.NESTED, RemoteMode.FLAT, RemoteMode.LIVE)
    override val remoteModeProperty: ObservableProperty<RemoteMode> = property(RemoteMode.NESTED)

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, Planet>? {
        return withContext(Dispatchers.Default) {
            try {
                val result = server.getPlanet(ID(id)).okOrThrow()
                RemoteMetadata.Planet(result.planet.name, result.lastModified) to result.planet
            } catch (e: Exception) {
                logger.error("Exception during Planet-Loading", id, e)
                null
            }
        }
    }

    override suspend fun savePlanet(id: String, planet: Planet): RemoteIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                server.putPlanet(ID(id), planet).okOrThrow()
                RemoteIdentifier(id, loadPlanet(id)?.first ?: return@withContext null)
            } catch (e: Exception) {
                logger.error("Exception during Planet-Saving", id, e)
                null
            }
        }
    }

    override suspend fun createPlanet(parentId: String, planet: Planet): RemoteIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                val info = server.postPlanet(planet).okOrThrow().info
                RemoteIdentifier(info.id.toString(), RemoteMetadata.Planet(info.name, info.lastModified))
            } catch (e: Exception) {
                logger.error("Exception during Planet-Creation", parentId, e)
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
                logger.error("Exception during Planet-Deletion", id, e)
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
                logger.error("Could not get Planet-List", id, e)
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
                logger.error("Could not search planets",e)
                null
            }
        }
    }
}
