package de.robolab.common.utils

import de.robolab.client.net.http
import de.robolab.client.net.web

actual fun getBuildInformation(): String? {
    return null
}

actual suspend fun getAsyncBuildInformation(): String {
    return http {
        web("build.ini")
    }.exec().body ?: ""
}
