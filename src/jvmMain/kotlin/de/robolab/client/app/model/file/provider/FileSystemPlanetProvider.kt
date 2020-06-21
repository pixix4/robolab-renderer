package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.WatchKey
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class FileSystemPlanetLoader(
    private val baseDirectory: File
) : IFilePlanetLoader<FileSystemPlanetLoader.FileIdentifier>, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override val onRemoteChange = EventHandler<Unit>()

    override val name = "Directory"

    override val desc = baseDirectory.absolutePath

    override val icon = MaterialIcon.FOLDER_OPEN

    override suspend fun loadContent(identifier: FileIdentifier): Pair<FileIdentifier, List<String>>? {
        return try {
            val lines = identifier.file.readLines()

            FileIdentifier(identifier.file) to lines
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveContent(identifier: FileIdentifier, lines: List<String>): FileIdentifier? {
        return try {
            identifier.file.writeText(lines.joinToString("\n"))

            FileIdentifier(identifier.file)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loadIdentifierList(): List<FileIdentifier> {
        return try {
            baseDirectory.listFiles()?.map { FileIdentifier(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    class FileIdentifier(
        val file: File,
        override val lastModified: DateTime = file.lastModified
    ) : IFilePlanetIdentifier {

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
    }

    init {
        if (baseDirectory.exists()) {
            thread {
                val watchService = FileSystems.getDefault().newWatchService()
                val folder = baseDirectory.toPath()

                folder.register(
                    watchService,
                    ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )

                var key: WatchKey = watchService.take()
                while (true) {
                    for (event in key.pollEvents()) {
                        val name = event.context()
                        if (name is Path) {
                            try {
                                onRemoteChange.emit()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
    get() = DateTime.fromUnix(lastModified())