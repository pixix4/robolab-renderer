package de.robolab.common.externaljs.fs

import kotlin.js.Date

private val module = js("require('fs')")

external interface Stats {
    val size: Number
    val atime: Date
    val mtime: Date
    val ctime: Date
    val atimeMs: Double
    val mtimeMs: Double
    val ctimeMs: Double

    fun isDirectory(): Boolean
    fun isFile(): Boolean
}

fun statSync(path: String): Stats {
    return module.statSync(path)
}

fun existsSync(path: String): Boolean {
    return module.existsSync(path)
}

fun readFileSync(path: String, options: dynamic): dynamic {
    return module.readFileSync(path, options)
}

fun writeFileSync(file: String, data: String) {
    module.writeFileSync(file, data)
}

fun watch(
    filename: String,
    options: dynamic,
    listener: (event: String, filename: String) -> Unit,
) {
    module.watch(filename, options, listener)
}
