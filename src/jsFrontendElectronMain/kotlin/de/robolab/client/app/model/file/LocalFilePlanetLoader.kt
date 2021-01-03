package de.robolab.client.app.model.file

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.common.externaljs.fs.*
import de.robolab.common.externaljs.toList
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext

class LocalFilePlanetLoader(
    val path: String
) : IFilePlanetLoader<LocalFilePlanetLoader.LocalFileIdentifier> {

    private val logger = Logger(this)

    override val onRemoteChange = EventHandler<LocalFileIdentifier?>()

    override val planetCountProperty = constObservable(0)

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(path)

    override val iconProperty = constObservable(MaterialIcon.CLOUD_QUEUE)

    override val availableProperty = property(true)
    var available by availableProperty

    override suspend fun loadPlanet(identifier: LocalFileIdentifier): Pair<LocalFileIdentifier, List<String>>? {
        return withContext(Dispatchers.Default) {
            try {
                val lines = readFile(identifier.id).await()
                available = true
                LocalFileIdentifier(identifier.id) to lines.split("\n")
            } catch (e: Exception) {
                logger.error("loadPlanet", e)
                available = false
                null
            }
        }
    }

    override suspend fun savePlanet(identifier: LocalFileIdentifier, lines: List<String>): LocalFileIdentifier? {
        return withContext(Dispatchers.Default) {
            try {
                writeFile(identifier.id, lines.joinToString("\n"))
                available = true
                LocalFileIdentifier(identifier.id)
            } catch (e: Exception) {
                logger.error("savePlanet", e)
                available = false
                null
            }
        }
    }

    override suspend fun createPlanet(identifier: LocalFileIdentifier?, lines: List<String>) {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                TODO()
            } catch (e: Exception) {
                logger.error("createPlanet", e)
                available = false
            }
        }
    }

    override suspend fun deletePlanet(identifier: LocalFileIdentifier) {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                unlink(identifier.id).await()
            } catch (e: Exception) {
                logger.error("deletePlanet", e)
                available = false
            }
        }
    }

    override suspend fun listPlanets(identifier: LocalFileIdentifier?): List<LocalFileIdentifier> {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                val dirList = readdir(identifier?.id ?: "").await().toList()
                dirList.map {
                    LocalFileIdentifier(it)
                }
            } catch (e: Exception) {
                logger.error("listPlanets", e)
                available = false
                emptyList()
            }
        }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<LocalFileIdentifier> {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                emptyList<LocalFileIdentifier>()
            } catch (e: Exception) {
                logger.error("searchPlanets", e)
                available = false
                emptyList()
            }
        }
    }

    companion object : IFilePlanetLoaderFactory {
        override fun create(uri: String): LocalFilePlanetLoader {
            return LocalFilePlanetLoader(uri)
        }
    }

    class LocalFileIdentifier(
        val id: String
    ) : IFilePlanetIdentifier {

        private val stats = statSync(id)

        override val isDirectory = stats.isDirectory()
        override val childrenCount = if (isDirectory) {
            readdirSync(id).size
        } else 0
        override val name = id
        override val lastModified: DateTime = DateTime(0L)
        override val path = emptyList<String>()
    }
}
