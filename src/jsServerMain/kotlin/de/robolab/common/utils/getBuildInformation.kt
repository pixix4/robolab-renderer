package de.robolab.common.utils

import de.robolab.server.externaljs.fs.existsSync
import de.robolab.server.externaljs.fs.readFileSync
import de.westermann.kobserve.base.ObservableValue

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
    return getBuildInformation() ?: ""
}

actual fun getRuntimeInformation(): List<Pair<String, ObservableValue<Any>>> {
    return emptyList()
}