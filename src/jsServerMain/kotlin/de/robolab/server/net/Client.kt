package de.robolab.server.net

import de.robolab.server.auth.User
import kotlinx.coroutines.Deferred

class Client(val id: String) {

    var user: User = User.Empty

    suspend fun oauthRedirect(url: String) : ReconnectInfo {
        TODO()
    }

    data class ReconnectInfo(val url: String, val params: Map<String, String>)
}