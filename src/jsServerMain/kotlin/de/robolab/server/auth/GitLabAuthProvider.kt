package de.robolab.server.auth

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import de.robolab.client.net.http
import de.robolab.client.net.sendHttpRequest
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.net.headers.Header
import de.robolab.server.RequestError
import de.robolab.server.`throw`
import de.robolab.server.config.Config
import de.robolab.server.externaljs.encodeURIComponent
import io.ktor.client.request.*
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.random.nextULong

class GitLabAuthProvider(private val callbackURL: String) {
    private val rand: Random = Random
    private val encodedCallbackURL: String = encodeURIComponent(callbackURL)
    private val apiHeader: AuthorizationHeader = AuthorizationHeader.Bearer(Config.Auth.gitlabAPIToken)

    suspend fun getRoboLabUserSet(): Set<UserID> {
        val response = http {
            url("${Config.Auth.gitlabURL}/api/v4/groups/9/members")
            header(apiHeader)
        }.exec()
        if (response.status != HttpStatusCode.Ok)
            response.`throw`()
        return response.jsonBody!!.jsonArray.map { it.jsonObject["id"]!!.jsonPrimitive.int.toUInt() }.toSet()
    }

    fun startAuthURL(shareCode: ShareCode): String {
        val state: UInt = shareCode

        return "${Config.Auth.gitlabURL}/oauth/authorize" +
                "?client_id=${Config.Auth.gitlabApplicationID}" +
                "&response_type=code" +
                "&state=$state" +
                "&scope=openid" +
                "&redirect_uri=$encodedCallbackURL"
    }

    suspend fun performAuth(code: String, state: String): User? {
        return performAuth(code, state, extractShareCode(state) ?: return null)
    }

    suspend fun performAuth(code: String, state: String, shareCode: ShareCode): User {
        val tokenResponse = http {
            url(
                "${Config.Auth.gitlabURL}/oauth/token" +
                        "?client_id=${Config.Auth.gitlabApplicationID}" +
                        "&client_secret=${Config.Auth.gitlabApplicationSecret}" +
                        "&code=$code" +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=$callbackURL"
            )
            body("")
            post()
        }.exec()
        if (tokenResponse.status != HttpStatusCode.Ok) {
            tokenResponse.`throw`()
        }
        val accessToken: String = tokenResponse.jsonBody!!.jsonObject["access_token"]!!.jsonPrimitive.content
        val authHeader: Header = AuthorizationHeader.Bearer(accessToken)
        val currentUserResponse = http {
            url("${Config.Auth.gitlabURL}/oauth/token/info")
            header(authHeader)
        }.exec()
        if (currentUserResponse.status != HttpStatusCode.Ok) {
            currentUserResponse.`throw`()
        }
        val robolabUserSet = getRoboLabUserSet()
        val currentUserId = currentUserResponse.jsonBody!!.jsonObject["resource_owner_id"]!!.jsonPrimitive.int.toUInt()
        return User(currentUserId, currentUserId in robolabUserSet)
    }

    fun extractShareCode(state: String): ShareCode? {
        return state.toUIntOrNull()
    }
}