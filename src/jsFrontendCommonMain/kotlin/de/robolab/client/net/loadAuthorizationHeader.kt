package de.robolab.client.net

import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.utils.Logger
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual suspend fun loadAuthorizationHeader(): AuthorizationHeader? {
    val tokenString = localStorage["robolab-token"] ?: return null
    return try {
        AuthorizationHeader.parse(tokenString)
    } catch (e: Exception) {
        Logger("loadAuthorizationHeader").error("Could not parse authorization-header",e)
        null
    }
}

actual suspend fun storeAuthorizationHeader(header: AuthorizationHeader?) {
    if (header == null) {
        localStorage.removeItem("robolab-token")
    } else {
        localStorage["robolab-token"] = header.toString()
    }
}

actual suspend fun loadRefreshToken(): String? {
    return localStorage["robolab-refresh"]
}

actual suspend fun storeRefreshToken(refreshToken: String?) {
    if (refreshToken == null) {
        localStorage.removeItem("robolab-refresh")
    } else {
        localStorage["robolab-refresh"] = refreshToken
    }
}
