@file:JsModule("fs")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs

import org.khronos.webgl.*
import org.w3c.dom.url.URL
import kotlin.js.Date
import kotlin.js.Promise

external open class Stats : StatsBase<Number> {
    override fun isFile(): Boolean
    override fun isDirectory(): Boolean
    override fun isBlockDevice(): Boolean
    override fun isCharacterDevice(): Boolean
    override fun isSymbolicLink(): Boolean
    override fun isFIFO(): Boolean
    override fun isSocket(): Boolean
    override var dev: Number
    override var ino: Number
    override var mode: Number
    override var nlink: Number
    override var uid: Number
    override var gid: Number
    override var rdev: Number
    override var size: Number
    override var blksize: Number
    override var blocks: Number
    override var atimeMs: Number
    override var mtimeMs: Number
    override var ctimeMs: Number
    override var birthtimeMs: Number
    override var atime: Date
    override var mtime: Date
    override var ctime: Date
    override var birthtime: Date
}

external open class Dirent {
    open fun isFile(): Boolean
    open fun isDirectory(): Boolean
    open fun isBlockDevice(): Boolean
    open fun isCharacterDevice(): Boolean
    open fun isSymbolicLink(): Boolean
    open fun isFIFO(): Boolean
    open fun isSocket(): Boolean
    open var name: String
}

external open class Dir {
    open var path: String
    open fun close(): Promise<Unit>
    open fun close(cb: NoParamCallback)
    open fun closeSync()

}

external fun rename(oldPath: String, newPath: String, callback: NoParamCallback)

external fun rename(oldPath: String, newPath: URL, callback: NoParamCallback)

external fun rename(oldPath: URL, newPath: String, callback: NoParamCallback)

external fun rename(oldPath: URL, newPath: URL, callback: NoParamCallback)

external fun renameSync(oldPath: String, newPath: String)

external fun renameSync(oldPath: String, newPath: URL)

external fun renameSync(oldPath: URL, newPath: String)

external fun renameSync(oldPath: URL, newPath: URL)

external fun truncate(path: String, len: Number?, callback: NoParamCallback)

external fun truncate(path: URL, len: Number?, callback: NoParamCallback)

external fun truncate(path: String, callback: NoParamCallback)

external fun truncate(path: URL, callback: NoParamCallback)

external fun truncateSync(path: String, len: Number? = definedExternally)

external fun truncateSync(path: URL, len: Number? = definedExternally)

external fun ftruncate(fd: Number, len: Number?, callback: NoParamCallback)

external fun ftruncate(fd: Number, callback: NoParamCallback)

external fun ftruncateSync(fd: Number, len: Number? = definedExternally)

external fun chown(path: String, uid: Number, gid: Number, callback: NoParamCallback)

external fun chown(path: URL, uid: Number, gid: Number, callback: NoParamCallback)

external fun chownSync(path: String, uid: Number, gid: Number)

external fun chownSync(path: URL, uid: Number, gid: Number)

external fun fchown(fd: Number, uid: Number, gid: Number, callback: NoParamCallback)

external fun fchownSync(fd: Number, uid: Number, gid: Number)

external fun lchown(path: String, uid: Number, gid: Number, callback: NoParamCallback)

external fun lchown(path: URL, uid: Number, gid: Number, callback: NoParamCallback)

external fun lchownSync(path: String, uid: Number, gid: Number)

external fun lchownSync(path: URL, uid: Number, gid: Number)

external fun chmod(path: String, mode: String, callback: NoParamCallback)

external fun chmod(path: String, mode: Number, callback: NoParamCallback)

external fun chmod(path: URL, mode: String, callback: NoParamCallback)

external fun chmod(path: URL, mode: Number, callback: NoParamCallback)

external fun chmodSync(path: String, mode: String)

external fun chmodSync(path: String, mode: Number)

external fun chmodSync(path: URL, mode: String)

external fun chmodSync(path: URL, mode: Number)

external fun fchmod(fd: Number, mode: String, callback: NoParamCallback)

external fun fchmod(fd: Number, mode: Number, callback: NoParamCallback)

external fun fchmodSync(fd: Number, mode: String)

external fun fchmodSync(fd: Number, mode: Number)

external fun lchmod(path: String, mode: String, callback: NoParamCallback)

external fun lchmod(path: String, mode: Number, callback: NoParamCallback)

external fun lchmod(path: URL, mode: String, callback: NoParamCallback)

external fun lchmod(path: URL, mode: Number, callback: NoParamCallback)

external fun lchmodSync(path: String, mode: String)

external fun lchmodSync(path: String, mode: Number)

external fun lchmodSync(path: URL, mode: String)

external fun lchmodSync(path: URL, mode: Number)

external fun statSync(path: String): Stats

external fun statSync(path: URL): Stats

external fun fstatSync(fd: Number): Stats

external fun lstatSync(path: String): Stats

external fun lstatSync(path: URL): Stats

external fun link(existingPath: String, newPath: String, callback: NoParamCallback)

external fun link(existingPath: String, newPath: URL, callback: NoParamCallback)

external fun link(existingPath: URL, newPath: String, callback: NoParamCallback)

external fun link(existingPath: URL, newPath: URL, callback: NoParamCallback)

external fun linkSync(existingPath: String, newPath: String)

external fun linkSync(existingPath: String, newPath: URL)

external fun linkSync(existingPath: URL, newPath: String)

external fun linkSync(existingPath: URL, newPath: URL)

external fun symlink(target: String, path: String, type: String /* "dir" | "file" | "junction" */, callback: NoParamCallback)

external fun symlink(target: String, path: URL, type: String /* "dir" | "file" | "junction" */, callback: NoParamCallback)

external fun symlink(target: URL, path: String, type: String /* "dir" | "file" | "junction" */, callback: NoParamCallback)

external fun symlink(target: URL, path: URL, type: String /* "dir" | "file" | "junction" */, callback: NoParamCallback)

external fun symlink(target: String, path: String, callback: NoParamCallback)

external fun symlink(target: String, path: URL, callback: NoParamCallback)

external fun symlink(target: URL, path: String, callback: NoParamCallback)

external fun symlink(target: URL, path: URL, callback: NoParamCallback)

external fun symlinkSync(target: String, path: String, type: String /* "dir" | "file" | "junction" */ = definedExternally)

external fun symlinkSync(target: String, path: URL, type: String /* "dir" | "file" | "junction" */ = definedExternally)

external fun symlinkSync(target: URL, path: String, type: String /* "dir" | "file" | "junction" */ = definedExternally)

external fun symlinkSync(target: URL, path: URL, type: String /* "dir" | "file" | "junction" */ = definedExternally)

external fun readlinkSync(path: String, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): String

external fun readlinkSync(path: URL, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): String

external fun readlinkSync(path: String, options: String? = definedExternally): dynamic /* String | Buffer */

external fun readlinkSync(path: URL, options: String? = definedExternally): dynamic /* String | Buffer */

external fun realpathSync(path: String, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): String

external fun realpathSync(path: URL, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): String

external fun realpathSync(path: String, options: String? = definedExternally): dynamic /* String | Buffer */

external fun realpathSync(path: URL, options: String? = definedExternally): dynamic /* String | Buffer */

external fun unlink(path: String, callback: NoParamCallback)

external fun unlink(path: URL, callback: NoParamCallback)

external fun unlinkSync(path: String)

external fun unlinkSync(path: URL)

external fun rmdir(path: String, callback: NoParamCallback)

external fun rmdir(path: URL, callback: NoParamCallback)

external fun rmdir(path: String, options: RmDirAsyncOptions, callback: NoParamCallback)

external fun rmdir(path: URL, options: RmDirAsyncOptions, callback: NoParamCallback)

external fun rmdirSync(path: String, options: RmDirOptions = definedExternally)

external fun rmdirSync(path: URL, options: RmDirOptions = definedExternally)

external fun mkdir(path: String, options: Number?, callback: NoParamCallback)

external fun mkdir(path: String, options: String?, callback: NoParamCallback)

external fun mkdir(path: String, options: MakeDirectoryOptions /* MakeDirectoryOptions & `T$34` */, callback: NoParamCallback)

external fun mkdir(path: URL, options: Number?, callback: NoParamCallback)

external fun mkdir(path: URL, options: String?, callback: NoParamCallback)

external fun mkdir(path: URL, options: MakeDirectoryOptions /* MakeDirectoryOptions & `T$34` */, callback: NoParamCallback)

external fun mkdir(path: String, callback: NoParamCallback)

external fun mkdir(path: URL, callback: NoParamCallback)

external fun mkdirSync(path: String, options: MakeDirectoryOptions /* MakeDirectoryOptions & `T$33` | MakeDirectoryOptions & `T$34` */): dynamic /* String | Unit */

external fun mkdirSync(path: URL, options: MakeDirectoryOptions /* MakeDirectoryOptions & `T$33` | MakeDirectoryOptions & `T$34` */): dynamic /* String | Unit */

external fun mkdirSync(path: String, options: Number? = definedExternally): dynamic /* Unit | String */

external fun mkdirSync(path: String, options: String? = definedExternally): dynamic /* Unit | String */

external fun mkdirSync(path: URL, options: Number? = definedExternally): dynamic /* Unit | String */

external fun mkdirSync(path: URL, options: String? = definedExternally): dynamic /* Unit | String */

external fun mkdirSync(path: String, options: MakeDirectoryOptions? = definedExternally): String?

external fun mkdirSync(path: URL, options: MakeDirectoryOptions? = definedExternally): String?

external fun mkdtempSync(prefix: String, options: `T$32`? = definedExternally): String

external fun mkdtempSync(prefix: String, options: String /* "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" | "buffer" */ = definedExternally): dynamic /* String | Buffer */

external fun mkdtempSync(prefix: String, options: String? = definedExternally): dynamic /* String | Buffer */

external fun readdirSync(path: String, options: dynamic /* `T$35`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): Array<String>

external fun readdirSync(path: URL, options: dynamic /* `T$35`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): Array<String>

external fun readdirSync(path: String, options: `T$37`? = definedExternally): dynamic /* Array<String> | Array<Buffer> */

external fun readdirSync(path: String, options: String? = definedExternally): dynamic /* Array<String> | Array<Buffer> */

external fun readdirSync(path: URL, options: `T$37`? = definedExternally): dynamic /* Array<String> | Array<Buffer> */

external fun readdirSync(path: URL, options: String? = definedExternally): dynamic /* Array<String> | Array<Buffer> */

external fun readdirSync(path: String, options: `T$38`): Array<Dirent>

external fun readdirSync(path: URL, options: `T$38`): Array<Dirent>

external fun close(fd: Number, callback: NoParamCallback)

external fun closeSync(fd: Number)

external fun openSync(path: String, flags: String, mode: String? = definedExternally): Number

external fun openSync(path: String, flags: String, mode: Number? = definedExternally): Number

external fun openSync(path: String, flags: Number, mode: String? = definedExternally): Number

external fun openSync(path: String, flags: Number, mode: Number? = definedExternally): Number

external fun openSync(path: URL, flags: String, mode: String? = definedExternally): Number

external fun openSync(path: URL, flags: String, mode: Number? = definedExternally): Number

external fun openSync(path: URL, flags: Number, mode: String? = definedExternally): Number

external fun openSync(path: URL, flags: Number, mode: Number? = definedExternally): Number

external fun utimes(path: String, atime: String, mtime: dynamic /* String | Number | Date */, callback: NoParamCallback)

external fun utimes(path: String, atime: Number, mtime: dynamic /* String | Number | Date */, callback: NoParamCallback)

external fun utimes(path: String, atime: Date, mtime: dynamic /* String | Number | Date */, callback: NoParamCallback)

external fun utimes(path: URL, atime: String, mtime: dynamic /* String | Number | Date */, callback: NoParamCallback)

external fun utimes(path: URL, atime: Number, mtime: dynamic /* String | Number | Date */, callback: NoParamCallback)

external fun utimes(path: URL, atime: Date, mtime: dynamic /* String | Number | Date */, callback: NoParamCallback)

external fun utimesSync(path: String, atime: String, mtime: dynamic /* String | Number | Date */)

external fun utimesSync(path: String, atime: Number, mtime: dynamic /* String | Number | Date */)

external fun utimesSync(path: String, atime: Date, mtime: dynamic /* String | Number | Date */)

external fun utimesSync(path: URL, atime: String, mtime: dynamic /* String | Number | Date */)

external fun utimesSync(path: URL, atime: Number, mtime: dynamic /* String | Number | Date */)

external fun utimesSync(path: URL, atime: Date, mtime: dynamic /* String | Number | Date */)

external fun futimes(fd: Number, atime: String, mtime: String, callback: NoParamCallback)

external fun futimes(fd: Number, atime: String, mtime: Number, callback: NoParamCallback)

external fun futimes(fd: Number, atime: String, mtime: Date, callback: NoParamCallback)

external fun futimes(fd: Number, atime: Number, mtime: String, callback: NoParamCallback)

external fun futimes(fd: Number, atime: Number, mtime: Number, callback: NoParamCallback)

external fun futimes(fd: Number, atime: Number, mtime: Date, callback: NoParamCallback)

external fun futimes(fd: Number, atime: Date, mtime: String, callback: NoParamCallback)

external fun futimes(fd: Number, atime: Date, mtime: Number, callback: NoParamCallback)

external fun futimes(fd: Number, atime: Date, mtime: Date, callback: NoParamCallback)

external fun futimesSync(fd: Number, atime: String, mtime: String)

external fun futimesSync(fd: Number, atime: String, mtime: Number)

external fun futimesSync(fd: Number, atime: String, mtime: Date)

external fun futimesSync(fd: Number, atime: Number, mtime: String)

external fun futimesSync(fd: Number, atime: Number, mtime: Number)

external fun futimesSync(fd: Number, atime: Number, mtime: Date)

external fun futimesSync(fd: Number, atime: Date, mtime: String)

external fun futimesSync(fd: Number, atime: Date, mtime: Number)

external fun futimesSync(fd: Number, atime: Date, mtime: Date)

external fun fsync(fd: Number, callback: NoParamCallback)

external fun fsyncSync(fd: Number)

external fun writeSync(fd: Number, buffer: Uint8Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Uint8ClampedArray, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Uint16Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Uint32Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Int8Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Int16Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Int32Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Float32Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: Float64Array, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, buffer: DataView, offset: Number? = definedExternally, length: Number? = definedExternally, position: Number? = definedExternally): Number

external fun writeSync(fd: Number, string: Any, position: Number? = definedExternally, encoding: String? = definedExternally): Number

external fun readSync(fd: Number, buffer: Uint8Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Uint8ClampedArray, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Uint16Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Uint32Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Int8Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Int16Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Int32Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Float32Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Float64Array, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: DataView, offset: Number, length: Number, position: Number?): Number

external fun readSync(fd: Number, buffer: Uint8Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Uint8ClampedArray, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Uint16Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Uint32Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Int8Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Int16Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Int32Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Float32Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: Float64Array, opts: ReadSyncOptions = definedExternally): Number

external fun readSync(fd: Number, buffer: DataView, opts: ReadSyncOptions = definedExternally): Number

external fun readFileSync(path: String, options: `T$43`): String

external fun readFileSync(path: String, options: String): String

external fun readFileSync(path: URL, options: `T$43`): String

external fun readFileSync(path: URL, options: String): String

external fun readFileSync(path: Number, options: `T$43`): String

external fun readFileSync(path: Number, options: String): String

external fun readFileSync(path: String, options: `T$44`? = definedExternally): dynamic /* String | Buffer */

external fun readFileSync(path: String, options: String? = definedExternally): dynamic /* String | Buffer */

external fun readFileSync(path: URL, options: `T$44`? = definedExternally): dynamic /* String | Buffer */

external fun readFileSync(path: URL, options: String? = definedExternally): dynamic /* String | Buffer */

external fun readFileSync(path: Number, options: `T$44`? = definedExternally): dynamic /* String | Buffer */

external fun readFileSync(path: Number, options: String? = definedExternally): dynamic /* String | Buffer */

external fun writeFile(path: String, data: Any, options: `T$45`?, callback: NoParamCallback)

external fun writeFile(path: String, data: Any, options: String?, callback: NoParamCallback)

external fun writeFile(path: URL, data: Any, options: `T$45`?, callback: NoParamCallback)

external fun writeFile(path: URL, data: Any, options: String?, callback: NoParamCallback)

external fun writeFile(path: Number, data: Any, options: `T$45`?, callback: NoParamCallback)

external fun writeFile(path: Number, data: Any, options: String?, callback: NoParamCallback)

external fun writeFile(path: String, data: Any, callback: NoParamCallback)

external fun writeFile(path: URL, data: Any, callback: NoParamCallback)

external fun writeFile(path: Number, data: Any, callback: NoParamCallback)

external fun writeFileSync(path: String, data: Any, options: `T$45`? = definedExternally)

external fun writeFileSync(path: String, data: Any, options: String? = definedExternally)

external fun writeFileSync(path: URL, data: Any, options: `T$45`? = definedExternally)

external fun writeFileSync(path: URL, data: Any, options: String? = definedExternally)

external fun writeFileSync(path: Number, data: Any, options: `T$45`? = definedExternally)

external fun writeFileSync(path: Number, data: Any, options: String? = definedExternally)

external fun appendFile(file: String, data: Any, options: `T$45`?, callback: NoParamCallback)

external fun appendFile(file: String, data: Any, options: String?, callback: NoParamCallback)

external fun appendFile(file: URL, data: Any, options: `T$45`?, callback: NoParamCallback)

external fun appendFile(file: URL, data: Any, options: String?, callback: NoParamCallback)

external fun appendFile(file: Number, data: Any, options: `T$45`?, callback: NoParamCallback)

external fun appendFile(file: Number, data: Any, options: String?, callback: NoParamCallback)

external fun appendFile(file: String, data: Any, callback: NoParamCallback)

external fun appendFile(file: URL, data: Any, callback: NoParamCallback)

external fun appendFile(file: Number, data: Any, callback: NoParamCallback)

external fun appendFileSync(file: String, data: Any, options: `T$45`? = definedExternally)

external fun appendFileSync(file: String, data: Any, options: String? = definedExternally)

external fun appendFileSync(file: URL, data: Any, options: `T$45`? = definedExternally)

external fun appendFileSync(file: URL, data: Any, options: String? = definedExternally)

external fun appendFileSync(file: Number, data: Any, options: `T$45`? = definedExternally)

external fun appendFileSync(file: Number, data: Any, options: String? = definedExternally)

external fun watchFile(filename: String, options: `T$46`?, listener: (curr: Stats, prev: Stats) -> Unit)

external fun watchFile(filename: URL, options: `T$46`?, listener: (curr: Stats, prev: Stats) -> Unit)

external fun watchFile(filename: String, listener: (curr: Stats, prev: Stats) -> Unit)

external fun watchFile(filename: URL, listener: (curr: Stats, prev: Stats) -> Unit)

external fun unwatchFile(filename: String, listener: (curr: Stats, prev: Stats) -> Unit = definedExternally)

external fun unwatchFile(filename: URL, listener: (curr: Stats, prev: Stats) -> Unit = definedExternally)

external fun watch(filename: String, options: dynamic /* `T$47`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */, listener: (event: String, filename: String) -> Unit = definedExternally): FSWatcher

external fun watch(filename: URL, options: dynamic /* `T$47`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */, listener: (event: String, filename: String) -> Unit = definedExternally): FSWatcher

external fun watch(filename: String, options: `T$49`?, listener: (event: String, filename: dynamic /* String | Buffer */) -> Unit = definedExternally): FSWatcher

external fun watch(filename: String, options: String?, listener: (event: String, filename: dynamic /* String | Buffer */) -> Unit = definedExternally): FSWatcher

external fun watch(filename: URL, options: `T$49`?, listener: (event: String, filename: dynamic /* String | Buffer */) -> Unit = definedExternally): FSWatcher

external fun watch(filename: URL, options: String?, listener: (event: String, filename: dynamic /* String | Buffer */) -> Unit = definedExternally): FSWatcher

external fun watch(filename: String, listener: (event: String, filename: String) -> Any = definedExternally): FSWatcher

external fun watch(filename: URL, listener: (event: String, filename: String) -> Any = definedExternally): FSWatcher

external fun exists(path: String, callback: (exists: Boolean) -> Unit)

external fun exists(path: URL, callback: (exists: Boolean) -> Unit)

external fun existsSync(path: String): Boolean

external fun existsSync(path: URL): Boolean

external fun access(path: String, mode: Number?, callback: NoParamCallback)

external fun access(path: URL, mode: Number?, callback: NoParamCallback)

external fun access(path: String, callback: NoParamCallback)

external fun access(path: URL, callback: NoParamCallback)

external fun accessSync(path: String, mode: Number = definedExternally)

external fun accessSync(path: URL, mode: Number = definedExternally)

external fun fdatasync(fd: Number, callback: NoParamCallback)

external fun fdatasyncSync(fd: Number)

external fun copyFile(src: String, dest: String, callback: NoParamCallback)

external fun copyFile(src: String, dest: URL, callback: NoParamCallback)

external fun copyFile(src: URL, dest: String, callback: NoParamCallback)

external fun copyFile(src: URL, dest: URL, callback: NoParamCallback)

external fun copyFile(src: String, dest: String, flags: Number, callback: NoParamCallback)

external fun copyFile(src: String, dest: URL, flags: Number, callback: NoParamCallback)

external fun copyFile(src: URL, dest: String, flags: Number, callback: NoParamCallback)

external fun copyFile(src: URL, dest: URL, flags: Number, callback: NoParamCallback)

external fun copyFileSync(src: String, dest: String, flags: Number = definedExternally)

external fun copyFileSync(src: String, dest: URL, flags: Number = definedExternally)

external fun copyFileSync(src: URL, dest: String, flags: Number = definedExternally)

external fun copyFileSync(src: URL, dest: URL, flags: Number = definedExternally)

external fun writevSync(fd: Number, buffers: Array<dynamic /* Uint8Array | Uint8ClampedArray | Uint16Array | Uint32Array | Int8Array | Int16Array | Int32Array | Float32Array | Float64Array | DataView */>, position: Number = definedExternally): Number

external fun readvSync(fd: Number, buffers: Array<dynamic /* Uint8Array | Uint8ClampedArray | Uint16Array | Uint32Array | Int8Array | Int16Array | Int32Array | Float32Array | Float64Array | DataView */>, position: Number = definedExternally): Number

external fun opendirSync(path: String, options: OpenDirOptions = definedExternally): Dir
