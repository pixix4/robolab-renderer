package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class FileSystemPlanetLoader(
    private val baseDirectory: File
) : IFilePlanetLoader<FileSystemPlanetLoader.FileIdentifier>, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override val onRemoteChange = EventHandler<Unit>()

    override val nameProperty = constObservable("Directory")

    override val descProperty = constObservable(baseDirectory.absolutePath)

    override val iconProperty = constObservable(MaterialIcon.FOLDER_OPEN)

    override val availableProperty = constObservable(true)

    private fun getFileNameOfLines(lines: List<String>): String {
        return (PlanetFile.getName(lines) ?: "").replace(' ', '_')
    }

    private fun findUnusedFile(name: String, vararg exclude: File): File {
        var count = 0
        var file = baseDirectory.resolve("$name.planet")
        while (file.exists() && file !in exclude) {
            count += 1
            file = baseDirectory.resolve("$name ($count).planet")
        }
        return file
    }

    override suspend fun loadPlanet(identifier: FileIdentifier): Pair<FileIdentifier, List<String>>? {
        return try {
            val lines = identifier.file.readLines()

            FileIdentifier(identifier.file) to lines
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun savePlanet(identifier: FileIdentifier, lines: List<String>): FileIdentifier? {
        val name = getFileNameOfLines(lines)
        return try {
            identifier.file.writeText(lines.joinToString("\n"))

            val file = findUnusedFile(name, identifier.file)
            identifier.file.renameTo(file)

            FileIdentifier(identifier.file)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createPlanet(lines: List<String>) {
        val name = getFileNameOfLines(lines)
        try {
            val file = findUnusedFile(name)
            file.writeText(lines.joinToString("\n"))
        } catch (e: Exception) {

        }
    }

    override suspend fun deletePlanet(identifier: FileIdentifier) {
        try {
            identifier.file.delete()
        } catch (e: Exception) {

        }
    }

    private fun listPlanetFiles(base: File, recursive: Boolean): List<File> {
        val filteredPlanets = base.listFiles()?.filter {
            (it.isDirectory && !it.name.startsWith(".")) || it.extension.endsWith("planet", true)
        } ?: emptyList()

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

    override suspend fun listPlanets(identifier: FileIdentifier?): List<FileIdentifier> {
        return try {
            listPlanetFiles(identifier?.file ?: baseDirectory, false)
                .map { FileIdentifier(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getPlanetNameOfFile(file: File): String? {
        return try {
            PlanetFile.getName(file.readText())
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchPlanets(search: String): List<FileIdentifier> {
        return try {
            listPlanetFiles(baseDirectory, true)
                .map { (getPlanetNameOfFile(it) ?: "") to it }
                .filter { (name, _) ->
                    name.contains(search, true)
                }
                .sortedBy { (name, _) ->
                    name.length
                }
                .map { (_, file) ->
                    FileIdentifier(file)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    class FileIdentifier(
        val file: File,
        override val lastModified: DateTime = file.lastModified
    ) : IFilePlanetIdentifier {

        override val isDirectory: Boolean
            get() = file.isDirectory

        override val name: String
            get() = file.name

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FileIdentifier) return false

            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            return file.hashCode()
        }

        override fun toString(): String {
            return file.absolutePath
        }
    }

    private fun emitRemoteChange() {
        synchronized(onRemoteChange) {
            onRemoteChange.emit()
        }
    }

    init {
        if (baseDirectory.exists()) {
            thread {
                val watchService = FileSystems.getDefault().newWatchService()
                val folder = baseDirectory.toPath()

                folder.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )

                var key: WatchKey = watchService.take()
                while (true) {
                    var shouldEmit = false
                    for (event in key.pollEvents()) {
                        val name = event.context()
                        if (name is Path) {
                            shouldEmit = true
                        }
                    }

                    if (shouldEmit) {
                        try {
                            emitRemoteChange()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    key.reset()
                    key = watchService.take()
                }
            }
        }
    }

    companion object : IFilePlanetLoaderFactory {

        override val protocol = "directory"

        override val usage: String = "$protocol:///path/to/the/planet/directory"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val path = uri.substringAfter("$protocol://")
            return FileSystemPlanetLoader(File(path))
        }
    }
}

val File.lastModified: DateTime
    get() = DateTime(lastModified().toDouble())
