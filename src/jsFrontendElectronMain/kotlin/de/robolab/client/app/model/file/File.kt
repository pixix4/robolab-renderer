package de.robolab.client.app.model.file

import de.robolab.common.externaljs.fs.*
import de.robolab.common.externaljs.path.pathParse
import de.robolab.common.externaljs.path.pathRelative
import de.robolab.common.externaljs.path.safeJoinPath
import de.robolab.common.externaljs.toList
import kotlinx.coroutines.await
import kotlinx.datetime.Instant

class File(val absolutePath: String) {

    private val pathObj by lazy {
        pathParse(absolutePath)
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
        statSync(absolutePath)
    }

    val isDirectory: Boolean
        get() = stats.isDirectory()

    val isFile: Boolean
        get() = stats.isFile()

    val lastModified: Instant
        get() = Instant.fromEpochMilliseconds(stats.mtimeMs.toLong())

    fun resolveChildren(vararg paths: String): File {
        return File(safeJoinPath(absolutePath, *paths))
    }

    fun relative(other: File): String {
        return pathRelative(absolutePath, other.absolutePath)
    }

    fun resolveSibling(vararg paths: String): File {
        return File(safeJoinPath(dir, *paths))
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
        val id = safeJoinPath(dir, relativeName)
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
