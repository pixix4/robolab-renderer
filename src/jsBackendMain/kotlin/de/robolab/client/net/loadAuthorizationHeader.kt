package de.robolab.client.net

import de.robolab.common.net.headers.AuthorizationHeader

actual suspend fun loadAuthorizationHeader(): AuthorizationHeader? {
    TODO("Not yet implemented")
}

actual suspend fun storeAuthorizationHeader(header: AuthorizationHeader?) {
}
actual suspend fun loadRefreshToken(): String? {
    TODO("Not yet implemented")
}

actual suspend fun storeRefreshToken(refreshToken: String?) {
}