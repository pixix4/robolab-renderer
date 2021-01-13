package de.robolab.client.app.model.file

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.common.externaljs.fs.*
import de.robolab.common.externaljs.toList
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import path.path
import kotlin.math.abs

class LocalFilePlanetLoader(
    private val baseDirectory: File
) : IFilePlanetLoader<LocalFilePlanetLoader.LocalFileIdentifier> {

    constructor(baseDirectoryName: String) : this(File(path.resolve(baseDirectoryName)))

    private val logger = Logger(this)

    override val onRemoteChange = EventHandler<LocalFileIdentifier?>()

    override val planetCountProperty = property(0)

    override val nameProperty = constObservable("Directory")

    override val descProperty = constObservable(baseDirectory.absolutePath)

    override val iconProperty = constObservable(MaterialIcon.FOLDER)

    override val availableProperty = constObservable(true)

    private fun getFileNameOfLines(lines: List<String>): String {
        return (PlanetFile.getName(lines) ?: "").replace(' ', '_')
    }

    private fun findUnusedFile(base: File, name: String, vararg exclude: File): File {
        var count = 0
        var file = base.resolveChildren("$name.planet")
        while (file.exists() && file !in exclude) {
            count += 1
            file = base.resolveChildren("$name ($count).planet")
        }
        return file
    }

    override suspend fun loadPlanet(identifier: LocalFileIdentifier): Pair<LocalFileIdentifier, List<String>>? {
        return try {
            val lines = identifier.file.readLines()
            identifier.update() to lines
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun savePlanet(identifier: LocalFileIdentifier, lines: List<String>): LocalFileIdentifier? {
        val name = getFileNameOfLines(lines)
        return try {
            identifier.file.writeLines(lines)

            val file = findUnusedFile(File(identifier.file.dir), name, identifier.file)
            if (file != identifier.file) {
                val f = identifier.file.renameAbsolute(file.absolutePath)
                return LocalFileIdentifier(f)
            }

            identifier.update()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createPlanet(identifier: LocalFileIdentifier?, lines: List<String>) {
        val name = getFileNameOfLines(lines)
        try {
            val file = findUnusedFile(identifier?.file ?: baseDirectory, name)
            file.writeLines(lines)
        } catch (e: Exception) {

        }
    }

    override suspend fun deletePlanet(identifier: LocalFileIdentifier) {
        try {
            identifier.file.delete()
        } catch (e: Exception) {

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

    override suspend fun listPlanets(identifier: LocalFileIdentifier?): List<LocalFileIdentifier> {
        return try {
            listPlanetFiles(identifier?.file ?: baseDirectory, false)
                .map { LocalFileIdentifier(it) }
                .sortedWith(compareBy<LocalFileIdentifier> {
                    !it.isDirectory
                }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                    it.name
                })
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getPlanetNameOfFile(file: File): String? {
        return try {
            PlanetFile.getName(file.readText())
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<LocalFileIdentifier> {
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
                    LocalFileIdentifier(file)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun updatePlanetCount() {
        planetCountProperty.value = listPlanetFiles(baseDirectory, true).size
    }

    private suspend fun emitRemoteChange(file: File) {
        val event = if (file == baseDirectory) null else LocalFileIdentifier(file)
        synchronized(onRemoteChange) {
            onRemoteChange.emit(event)
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

        val del: String = path.sep

        fun rel(from: String, to: String): String = path.relative(from, to)
    }

    inner class LocalFileIdentifier(
        val file: File
    ) : IFilePlanetIdentifier {


        override val isDirectory = file.isDirectory
        override val childrenCount = if (isDirectory) {
            readdirSync(file.absolutePath).size
        } else 0
        override val name = file.name
        override val lastModified: DateTime = file.lastModified
        override val path = rel(file.dir, baseDirectory.absolutePath).split(del)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is LocalFileIdentifier) return false

            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            return file.hashCode()
        }

        override fun toString(): String {
            return "LocalFileIdentifier($file)"
        }

        fun update() = LocalFileIdentifier(File(file.absolutePath))
    }
}

class File(val absolutePath: String) {

    private val pathObj by lazy {
        path.parse(absolutePath)
    }

    val dir: String
        get() = pathObj.dir
    val base: String
        get() = pathObj.base
    val name: String
        get() = pathObj.name
    val ext: String
        get() = pathObj.ext


    private val stats by lazy {
        fs.statSync(absolutePath)
    }

    val isDirectory: Boolean
        get() = stats.isDirectory()

    val isFile: Boolean
        get() = stats.isFile()

    val lastModified: DateTime
        get() = DateTime.fromUnix(stats.mtimeMs.toLong())

    fun resolveChildren(vararg paths: String): File {
        return File(path.join(absolutePath, *paths))
    }

    fun resolveSibling(vararg paths: String): File {
        return File(path.join(dir, *paths))
    }

    val parent: File
        get() = File(dir)

   suspend fun createDirectories() {
        mkdir(absolutePath, true).await()
    }

    fun exists(): Boolean {
        return existsSync(absolutePath)
    }

    suspend fun listFiles(): List<File> {
        val children = readdir(absolutePath).await().toList()
        return children.map { resolveChildren(it) }
    }

    suspend fun readText(): String {
        return readFile(absolutePath).await()
    }

    suspend fun writeText(text: String) {
        writeFile(absolutePath, text).await()
    }

    suspend fun delete() {
        unlink(absolutePath).await()
    }

    suspend fun renameRelative(relativeName: String): File {
        val id = path.join(dir, relativeName)
        rename(absolutePath, id).await()
        return File(id)
    }

    suspend fun renameAbsolute(absoluteName: String): File {
        rename(absolutePath, absoluteName).await()
        return File(absoluteName)
    }

    fun readTextSync(): String {
        return readFileSync(absolutePath, null).toString()
    }

    fun writeTextSync(text: String) {
        writeFileSync(absolutePath, text)
    }

    override fun toString(): String {
        return absolutePath
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is File) return false

        if (absolutePath != other.absolutePath) return false

        return true
    }

    override fun hashCode(): Int {
        return absolutePath.hashCode()
    }
}

suspend fun File.readLines(): List<String> = readText().split("\n")
suspend fun File.writeLines(lines: List<String>) = writeText(lines.joinToString("\n"))

class FileSystemWatcher(baseDirectory: File) {

    val onFolderChange = EventHandler<File>()

    init {
        fs.watch(baseDirectory.absolutePath, object : fs.`T$48` {
            override var encoding: String = "utf8"
            override var persistent: Boolean? = false
            override var recursive: Boolean? = true
        }) { _, filename: String ->
            onFolderChange.emit(File(filename))
        }
    }
}
