package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.app.model.file.provider.RemoteIdentifier
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.fs.readdir
import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import path.path

class LocalFilePlanetLoader(
    private val baseDirectory: File
) : IFilePlanetLoader {

    constructor(baseDirectoryName: String) : this(File(path.resolve(baseDirectoryName)))

    override val onRemoteChange = EventHandler<RemoteIdentifier>()

    override val planetCountProperty = property(0)

    override val nameProperty = constObservable("Directory")

    override val descProperty = constObservable(baseDirectory.absolutePath)

    override val iconProperty = constObservable(MaterialIcon.FOLDER)

    override val availableProperty = constObservable(true)

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

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, List<String>>? {
        return try {
            val file = idToFile(id)
            val lines = file.readLines()
            val planet = PlanetFile(lines).planet
            RemoteMetadata.Planet(
                planet.name,
                file.lastModified,
                planet.getPointList().size
            ) to lines
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun savePlanet(id: String, lines: List<String>): RemoteIdentifier? {
        val file = idToFile(id)
        val planet = PlanetFile(lines).planet
        val name = planet.name.replace(' ', '_')
        return try {
            file.writeLines(lines)

            var newId = id

            val fi = findUnusedFile(File(file.dir), name, file)
            if (fi != file) {
                newId = fileToId(file.renameAbsolute(fi.absolutePath))
            }

            val metadata = RemoteMetadata.Planet(
                planet.name,
                file.lastModified,
                planet.getPointList().size
            )

            RemoteIdentifier(
                newId,
                metadata
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createPlanet(parentId: String, lines: List<String>): RemoteIdentifier? {
        val planet = PlanetFile(lines).planet
        val name = planet.name.replace(' ', '_')
        return try {
            val file = findUnusedFile(idToFile(parentId), name)
            file.writeLines(lines)

            RemoteIdentifier(
                fileToId(file),
                RemoteMetadata.Planet(
                    planet.name,
                    file.lastModified,
                    planet.getPointList().size
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deletePlanet(id: String): Boolean {
        return try {
            idToFile(id).delete()
            true
        } catch (e: Exception) {
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
            val planet = PlanetFile(file.readLines()).planet
            RemoteMetadata.Planet(
                planet.name,
                file.lastModified,
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
            null
        }
    }

    private suspend fun getPlanetNameOfFile(file: File): String? {
        return try {
            PlanetFile.getName(file.readText())
        } catch (e: Exception) {
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
