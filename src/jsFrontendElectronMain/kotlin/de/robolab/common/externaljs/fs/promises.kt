@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.common.externaljs.fs

import de.robolab.common.externaljs.JSArray
import de.robolab.common.externaljs.dynamicOf
import kotlin.js.Promise

private val module = js("require('fs')")

fun mkdir(path: String, recursive: Boolean): Promise<Unit> = module.promises.mkdir(path, dynamicOf("recursive" to recursive))

fun readdir(path: String): Promise<JSArray<String>> = module.promises.readdir(path)

fun readFile(path: String): Promise<String> = module.promises.readFile(path,"utf8")

fun rename(oldPath: String, newPath: String): Promise<Unit> = module.promises.rename(oldPath, newPath)
fun unlink(path: String): Promise<Unit> = module.promises.unlink(path)

fun writeFile(file: String, data: String): Promise<Unit> = module.promises.writeFile(file, data, "utf8")
