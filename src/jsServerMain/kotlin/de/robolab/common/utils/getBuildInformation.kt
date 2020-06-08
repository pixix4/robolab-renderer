package de.robolab.common.utils

import de.robolab.server.externaljs.fs.existsSync
import de.robolab.server.externaljs.fs.readFileSync

fun readFile(filename: String): String? {
    if (existsSync(filename)) {
        return readFileSync(filename, "utf8") as String
    }
    return null
}

actual fun getBuildInformation(): String? {
    return readFile("build.ini") ?: readFile("build/processedResources/build.ini")
}

actual suspend fun getAsyncBuildInformation(): String {
    TODO("Not yet implemented")
}