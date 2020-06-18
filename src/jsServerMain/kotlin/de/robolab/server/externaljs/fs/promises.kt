@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.server.externaljs.fs

import de.robolab.server.externaljs.Buffer
import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.dynamicOf
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.asPromise
import kotlin.js.Date
import kotlin.js.Promise

private val promises: dynamic = js("require(\"fs\").promises")

external interface FileHandleWriteResult {
    val bytesWritten: Int
    val buffer: Buffer
}

class FileHandle(private val fh: dynamic) {
    val fd: Int
        get() = fh.fd

    fun close(): Promise<Unit> = fh.close()
    fun readFile(encoding: String = "utf8"): Promise<String> = fh.readFile(encoding)
    fun readFileBuffer(): Promise<Buffer> = fh.readFile()
    fun stat(): Promise<Stats> = fh.stats()
    fun utimes(atime: Date, mtime: Date): Promise<Unit> = fh.utimes(atime, mtime)
    fun utimes(atime: Int, mtime: Int): Promise<Unit> = fh.utimes(atime, mtime)
    fun write(string: String): Promise<FileHandleWriteResult> = fh.write(string)
    fun write(string: String, position: Int): Promise<FileHandleWriteResult> = fh.write(string, position)
    fun write(string: String, position: Int, encoding: String = "utf8"): Promise<FileHandleWriteResult> =
        fh.write(string, position, encoding)

    fun writeFile(data: String): Promise<Unit> = fh.writeFile(data)
    fun writeFile(data: String, encoding: String = "utf8", flag: String = "w"): Promise<Unit> =
        fh.writeFile(data, dynamicOf("encoding" to encoding, "flag" to flag))
}

fun appendFile(path: String, data: String): Promise<Unit> = promises.appendFile(path, data)
fun appendFile(path: String, data: String, encoding: String): Promise<Unit> = promises.appendFile(path, data, encoding)
fun appendFile(path: FileHandle, data: String): Promise<Unit> = promises.appendFile(path, data)
fun appendFile(path: FileHandle, data: String, encoding: String): Promise<Unit> =
    promises.appendFile(path, data, encoding)

fun copyFile(src: String, dest: String): Promise<Unit> = promises.copyFile(src, dest)
fun copyFile(src: String, dest: String, flags: Int) = promises.copyFile(src, dest, flags)

fun mkdir(path: String): Promise<Unit> = promises.mkdir(path)
fun mkdir(path: String, recursive: Boolean): Promise<Unit> = promises.mkdir(path, dynamicOf("recursive" to recursive))
fun mkdtemp(prefix: String): Promise<String> = promises.mkdtemp(prefix)
fun mkdtemp(prefix: String, encoding: String): Promise<String> = promises.mkdtemp(prefix, encoding)

fun open(path: String): Promise<FileHandle> = (promises.open(path) as Promise<dynamic>).then(::FileHandle)
fun open(path: String, flags: String): Promise<FileHandle> =
    (promises.open(path, flags) as Promise<dynamic>).then(::FileHandle)

fun open(path: String, flags: Int): Promise<FileHandle> =
    (promises.open(path, flags) as Promise<dynamic>).then(::FileHandle)

fun readdir(path: String): Promise<JSArray<String>> = promises.readdir(path)
fun readdir(path: String, encoding: String): Promise<JSArray<String>> = promises.readdir(path, encoding)
fun readdirents(path: String): Promise<JSArray<Dirent>> = promises.readdir(path, dynamicOf("withFileTypes" to true))
fun readdirents(path: String, encoding: String): Promise<JSArray<Dirent>> =
    promises.readdir(path, dynamicOf("withFileTypes" to true, "encoding" to encoding))

fun readFile(path: String): Promise<String> = promises.readFile(path,"utf8")
fun readFile(path: FileHandle): Promise<String> = promises.readFile(path,"utf8")
fun readFile(path: String, encoding: String): Promise<String> = promises.readFile(path, encoding)
fun readFile(path: FileHandle, encoding: String): Promise<String> = promises.readFile(path, encoding)

fun rename(oldPath: String, newPath: String): Promise<Unit> = promises.rename(oldPath, newPath)
fun rmdir(path: String): Promise<Unit> = promises.rmdir(path)
fun stat(path: String): Promise<Stats> = promises.stat(path)
fun unlink(path: String): Promise<Unit> = promises.unlink(path)
fun utimes(path: String, atime: Date, mtime: Date): Promise<Unit> = promises.utimes(path, atime, mtime)
fun utimes(path: String, atime: Int, mtime: Int): Promise<Unit> = promises.utimes(path, atime, mtime)

fun writeFile(file: String, data: String): Promise<Unit> = promises.writeFile(file, data, "utf8")
fun writeFile(file: String, data: String, encoding: String): Promise<Unit> = promises.writeFile(file, data, encoding)
fun writeFile(file: String, data: String, encoding: String, flag: String): Promise<Unit> =
    promises.writeFile(file, data, dynamicOf("encoding" to encoding, "flag" to flag))

fun writeFile(file: FileHandle, data: String): Promise<Unit> = promises.writeFile(file, data, "utf8")
fun writeFile(file: FileHandle, data: String, encoding: String): Promise<Unit> =
    promises.writeFile(file, data, encoding)

fun writeFile(file: FileHandle, data: String, encoding: String, flag: String): Promise<Unit> =
    promises.writeFile(file, data, dynamicOf("encoding" to encoding, "flag" to flag))