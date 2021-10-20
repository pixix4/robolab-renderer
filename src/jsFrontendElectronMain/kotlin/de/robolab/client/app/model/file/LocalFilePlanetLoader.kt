package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.*
import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.fs.readdir
import de.robolab.common.externaljs.path.pathResolve
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetFile
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

class LocalFilePlanetLoader(
    private val baseDirectory: File
) : IFilePlanetLoader {

    val logger = Logger(this)

    override val id = "local-file-loader-${baseDirectory.absolutePath}"

    constructor(baseDirectoryName: String) : this(File(pathResolve(baseDirectoryName)))

    override val onRemoteChange = EventHandler<RemoteIdentifier>()

    override val planetCountProperty = property(0)

    override val nameProperty = constObservable("Directory")

    override val descProperty = constObservable(baseDirectory.absolutePath)

    override val iconProperty = constObservable(MaterialIcon.FOLDER)

    override val availableProperty = constObservable(true)

    override val supportedRemoteModes: List<RemoteMode> = listOf(RemoteMode.NESTED)
    override val remoteModeProperty: ObservableProperty<RemoteMode> = property(RemoteMode.NESTED)


    private fun findUnusedFile(base: File, name: String, vararg exclude: File): File {
        var count = 0
        var file = base.resolveChildren("$name.planet")
        while (file.exists() && file !in exclude) {
            count += 1
            file = base.resolveChildren("$name ($count).planet")
        }
        return file
    }

    private fun idToFile(id: String): File {
        return baseDirectory.resolveChildren(id)
    }

    private fun fileToId(file: File): String {
        return baseDirectory.relative(file)
    }

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, Planet>? {
        return try {
            val file = idToFile(id)
            val lines = file.readText()
            val planet = PlanetFile(lines).planet
            RemoteMetadata.Planet(
                planet.name,
                file.lastModified,
                planet.tags,
                planet.getPointList().size
            ) to planet
        } catch (e: Exception) {
            logger.error("Could not load planet",id,e)
            null
        }
    }

    override suspend fun savePlanet(id: String, planet: Planet): RemoteIdentifier? {
        val file = idToFile(id)
        val name = planet.name.replace(' ', '_')
        return try {
            file.writeText(PlanetFile.stringify(planet))

            var newId = id

            val fi = findUnusedFile(File(file.dir), name, file)
            if (fi != file) {
                newId = fileToId(file.renameAbsolute(fi.absolutePath))
            }

            val metadata = RemoteMetadata.Planet(
                planet.name,
                file.lastModified,
                planet.tags,
                planet.getPointList().size
            )

            RemoteIdentifier(
                newId,
                metadata
            )
        } catch (e: Exception) {
            logger.error("Could not save planet",id,e)
            null
        }
    }

    override suspend fun createPlanet(parentId: String, planet: Planet): RemoteIdentifier? {
        val name = planet.name.replace(' ', '_')
        return try {
            val file = findUnusedFile(idToFile(parentId), name)
            file.writeText(PlanetFile.stringify(planet))

            RemoteIdentifier(
                fileToId(file),
                RemoteMetadata.Planet(
                    planet.name,
                    file.lastModified,
                    planet.tags,
                    planet.getPointList().size
                )
            )
        } catch (e: Exception) {
            logger.error("Exception during Planet-Creation", parentId, e)
            null
        }
    }

    override suspend fun deletePlanet(id: String): Boolean {
        return try {
            idToFile(id).delete()
            true
        } catch (e: Exception) {
            logger.error("Exception during Planet-Deletion", id, e)
            false
        }
    }

    private suspend fun listPlanetFiles(base: File, recursive: Boolean): List<File> {
        val filteredPlanets = base.listFiles().filter {
            (it.isDirectory && !it.name.startsWith(".")) && it.name != "scripts" || it.ext.endsWith("planet", true)
        }

        return if (recursive) {
            filteredPlanets.flatMap {
                if (it.isDirectory) {
                    listPlanetFiles(it, true)
                } else {
                    listOf(it)
                }
            }
        } else {
            filteredPlanets
        }
    }

    private suspend fun mapFile(file: File): RemoteIdentifier {
        val metadata = if (file.isDirectory) RemoteMetadata.Directory(
            file.name,
            file.lastModified,
            readdir(file.absolutePath).await().length
        ) else {
            val planet = PlanetFile(file.readText()).planet
            RemoteMetadata.Planet(
                planet.name,
                file.lastModified,
                planet.tags,
                planet.getPointList().size
            )
        }

        return RemoteIdentifier(
            fileToId(file),
            metadata
        )
    }

    override suspend fun listPlanets(id: String): List<RemoteIdentifier>? {
        val file = idToFile(id)
        return try {
            listPlanetFiles(file, false)
                .map { mapFile(it) }
                .sortedWith(compareBy<RemoteIdentifier> {
                    it.metadata is RemoteMetadata.Planet
                }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                    it.metadata.name
                })
        } catch (e: Exception) {
            logger.error("Could not get Planet-List", id, e)
            null
        }
    }

    private suspend fun getPlanetNameOfFile(file: File): String? {
        return try {
            PlanetFile.parse(file.readText()).name
        } catch (e: Exception) {
            logger.error("Exception during getting name of file", file.absolutePath, e)
            null
        }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<RemoteIdentifier>? {
        val filter: (Pair<String, File>) -> Boolean = if (matchExact) { (name, _) ->
            name == search
        } else { (name, _) ->
            name.contains(search, true)
        }

        return try {
            listPlanetFiles(baseDirectory, true)
                .map { (getPlanetNameOfFile(it) ?: "") to it }
                .filter(filter)
                .sortedBy { (name, _) ->
                    name.length
                }
                .map { (_, file) ->
                    mapFile(file)
                }
        } catch (e: Exception) {
            logger.error("Could not search planets",e)
            emptyList()
        }
    }

    private suspend fun updatePlanetCount() {
        planetCountProperty.value = listPlanetFiles(baseDirectory, true).size
    }

    private suspend fun emitRemoteChange(file: File) {
        synchronized(onRemoteChange) {
            onRemoteChange.emit(mapFile(file))
        }
        updatePlanetCount()
    }

    init {
        if (baseDirectory.exists()) {
            val watcher = FileSystemWatcher(baseDirectory)

            watcher.onFolderChange {
                GlobalScope.launch {
                    emitRemoteChange(it)
                }
            }

            GlobalScope.launch {
                updatePlanetCount()
            }
        }
    }

    companion object : IFilePlanetLoaderFactory {
        override fun create(uri: String): LocalFilePlanetLoader? {
            if (uri.isNotBlank() && existsSync(uri)) {
                return LocalFilePlanetLoader(uri)
            }
            return null
        }
    }
}
