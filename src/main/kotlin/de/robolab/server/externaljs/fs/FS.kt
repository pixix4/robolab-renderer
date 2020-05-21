@file:JsModule("fs")
@file:JsNonModule
package de.robolab.server.externaljs.fs

import de.robolab.server.externaljs.JSBiDynErrorCallback
import de.robolab.server.externaljs.JSDynErrorCallback
import org.w3c.dom.events.Event
import kotlin.js.Date

external class Dirent{
    fun isDirectory(): Boolean
    fun isFile(): Boolean
    val name: String
}

external class Stats{
    val size: Number
    val atime: Date
    val mtime: Date
    val ctime: Date
    fun isDirectory(): Boolean
    fun isFile(): Boolean
}

external class ReadStream {
    val close: Event
    val open: Event
    val ready: Event
    val bytesRead: Int
    val path: String
    val pending: Boolean
}
external class WriteStream

external fun copyFile(src:String,dest:String,callback:JSDynErrorCallback) : Unit
external fun copyFile(src:String,dest:String,mode:Int, callback:JSDynErrorCallback) : Unit
external fun copyFileSync(src:String,dest:String) : Unit
external fun copyFileSync(src:String,dest:String,mode:Int) : Unit

external fun createReadStream(path: String): ReadStream
external fun createReadStream(path: String, options: dynamic): ReadStream

external fun createWriteStream(path: String): WriteStream
external fun createWriteStream(path: String, options: dynamic): WriteStream

external fun stat(path: String, callback: JSBiDynErrorCallback<Stats>): Unit
external fun statSync(path:String): Stats

external fun existsSync(path:String): Boolean

external fun mkdir(path:String, callback: JSDynErrorCallback): Unit
external fun mkdir(path:String, options:dynamic, callback: JSDynErrorCallback): Unit
external fun mkdirSync(path:String): String?
external fun mkdirSync(path:String, options:dynamic): String?

external fun mkdtemp(prefix:String, callback: JSBiDynErrorCallback<String>): Unit
external fun mkdtemp(prefix:String, options:dynamic, callback: JSBiDynErrorCallback<String>): Unit
external fun mkdtempSync(prefix:String): String
external fun mkdtempSync(prefix:String, options:dynamic): String

external fun open(path:String, callback:JSBiDynErrorCallback<Int>): Unit
external fun open(path:String, flags: String, callback:JSBiDynErrorCallback<Int>): Unit
external fun open(path:String, flags: Int, callback:JSBiDynErrorCallback<Int>): Unit
external fun open(path:String, flags: String, mode:String, callback:JSBiDynErrorCallback<Int>): Unit
external fun open(path:String, flags: String, mode:Int, callback:JSBiDynErrorCallback<Int>): Unit
external fun open(path:String, flags: Int, mode:String, callback:JSBiDynErrorCallback<Int>): Unit
external fun open(path:String, flags: Int, mode:Int, callback:JSBiDynErrorCallback<Int>): Unit
external fun openSync(path:String): Int
external fun openSync(path:String, flags: String): Int
external fun openSync(path:String, flags: Int): Int
external fun openSync(path:String, flags: String, mode:String): Int
external fun openSync(path:String, flags: String, mode:Int): Int
external fun openSync(path:String, flags: Int, mode:String): Int
external fun openSync(path:String, flags: Int, mode:Int): Int

external fun read(fd:Int, callback: JSBiDynErrorCallback<Int>):Unit
external fun read(fd:Int, options: dynamic, callback:JSBiDynErrorCallback<Int>):Unit

external fun readdir(path: String, callback: JSBiDynErrorCallback<Array<String>>): Unit
external fun readdir(path:String, options: dynamic, callback: JSBiDynErrorCallback<Array<dynamic>>): Unit
external fun readdirSync(path:String) : Array<String>
external fun readdirSync(path:String, options:dynamic): Array<dynamic>

external fun readFile(path:String, options:dynamic, callback:JSBiDynErrorCallback<dynamic>): Unit
external fun readFileSync(path:String, options:dynamic) : dynamic

external fun rename(oldPath: String, newPath:String, callback: JSDynErrorCallback): Unit
external fun renameSync(oldPath:String, newPath:String): Unit

external fun rmdir(path: String, callback:JSDynErrorCallback): Unit
external fun rmdir(path:String, options:dynamic, callback:JSDynErrorCallback): Unit
external fun rmdirSync(path: String): Unit
external fun rmdirSync(path:String, options:dynamic): Unit

external fun write(fd:Int, string:String, callback:(dynamic,Int,String)->Unit) : Unit
external fun write(fd:Int, string:String, position:Int, callback:(dynamic,Int,String)->Unit) : Unit
external fun write(fd:Int, string:String, position:Int, encoding: String, callback:(dynamic,Int,String)->Unit) : Unit
external fun writeSync(fd:Int, string:String) : Int
external fun writeSync(fd:Int, string:String, position:Int) : Int
external fun writeSync(fd:Int, string:String, position:Int, encoding: String) : Int

external fun writeFile(file:Int, data:String, callback:JSDynErrorCallback): Unit
external fun writeFile(file:Int, data:String, options: dynamic, callback:JSDynErrorCallback): Unit
external fun writeFile(file:String, data:String, callback:JSDynErrorCallback): Unit
external fun writeFile(file:String, data:String, options: dynamic, callback:JSDynErrorCallback): Unit
external fun writeFileSync(file:Int, data:String): Unit
external fun writeFileSync(file:Int, data:String, options: dynamic): Unit
external fun writeFileSync(file:String, data:String): Unit
external fun writeFileSync(file:String, data:String, options: dynamic): Unit
