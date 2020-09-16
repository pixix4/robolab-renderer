package de.robolab.client.app.model.file.provider

import com.sun.nio.file.SensitivityWatchEventModifier
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import javafx.application.Platform
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Consumer
import kotlin.concurrent.thread

class FileSystemWatcher(
    private val rootFolder: File
) {

    private val watcher = FileSystems.getDefault().newWatchService()!!

    init {
        startRecursiveWatcher()
    }

    val onFolderChange = EventHandler<File>()

    private fun startRecursiveWatcher() {
        val keys: MutableMap<WatchKey, Path> = HashMap()
        val register = Consumer { p: Path ->
            if (!p.toFile().exists() || !p.toFile().isDirectory) {
                throw RuntimeException("folder $p does not exist or is not a directory")
            }
            try {
                Files.walkFileTree(p, object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun preVisitDirectory(
                        dir: Path,
                        attrs: BasicFileAttributes
                    ): FileVisitResult {
                        val watchKey = dir.register(
                            watcher,
                            arrayOf<WatchEvent.Kind<*>>(
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE
                            ),
                            SensitivityWatchEventModifier.HIGH
                        )
                        keys[watchKey] = dir
                        return FileVisitResult.CONTINUE
                    }
                })
            } catch (e: IOException) {
                throw RuntimeException("Error registering path $p")
            }
        }

        register.accept(rootFolder.toPath())

        thread(name = "FileSystemWatcher", isDaemon = true) {
            while (true) {
                val key = try {
                    watcher.take()
                } catch (ex: InterruptedException) {
                    break
                }

                val dir = keys[key] ?: continue
                val changedFolders = key.pollEvents().asSequence()
                    .filter { e ->
                        e.kind() !== StandardWatchEventKinds.OVERFLOW
                    }
                    .mapNotNull { e ->
                        val context = e.context()
                        context as? Path
                    }
                    .map { p ->
                        dir.resolve(p).toFile().absoluteFile
                    }
                    .onEach { file ->
                        if (file.isDirectory) {
                            register.accept(file.toPath())
                        }
                    }
                    .map { file ->
                        file.parentFile
                    }
                    .distinct()
                    .toList()

                Platform.runLater {
                    for (folder in changedFolders) {
                        onFolderChange.emit(folder)
                    }
                }

                val valid = key.reset()
                if (!valid) {
                    break
                }
            }
        }
    }
}
