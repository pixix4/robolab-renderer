package de.robolab.server.auth

import de.robolab.client.net.http
import de.robolab.common.auth.AccessLevel
import de.robolab.common.auth.User
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.net.headers.Header
import de.robolab.common.net.`throw`
import de.robolab.server.config.Config
import de.robolab.common.externaljs.encodeURIComponent
import de.robolab.server.net.gitlab.GitLabServer
import de.robolab.server.net.gitlab.getGroupIDsForUser
import de.robolab.server.net.gitlab.getRoboLabTutors
import de.robolab.server.net.gitlab.getUserInfo
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random

class GitLabAuthProvider(private val callbackURL: String) {
    private val rand: Random = Random
    private val encodedCallbackURL: String = encodeURIComponent(callbackURL)

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

    @Suppress("UNUSED_PARAMETER")
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
        val tokenInfoResponse = http {
            url("${Config.Auth.gitlabURL}/oauth/token/info")
            header(authHeader)
        }.exec()
        if (tokenInfoResponse.status != HttpStatusCode.Ok) {
            tokenInfoResponse.`throw`()
        }
        val robolabTutorSet = GitLabServer.getRoboLabTutors().keys
        val currentUserId = tokenInfoResponse.jsonBody!!.jsonObject["resource_owner_id"]!!.jsonPrimitive.int.toUInt()
        val userInfo = GitLabServer.getUserInfo(currentUserId)
        var currentAccessLevel = if (currentUserId in robolabTutorSet) AccessLevel.Tutor else AccessLevel.LoggedIn
        val groupID: Int?
        if (currentAccessLevel satisfies AccessLevel.Tutor) {
            groupID = null
        } else {
            val groupIDs = GitLabServer.getGroupIDsForUser(currentUserId)
            if (groupIDs.isEmpty()) {
                groupID = null
            } else {
                groupID = groupIDs.firstOrNull()
                currentAccessLevel = AccessLevel.GroupMember
                if (groupIDs.size > 1)
                    console.error("User#$currentUserId is part of multiple groups: $groupIDs")
            }
        }
        return User(currentUserId, currentAccessLevel, groupID, userInfo.username)
    }

    fun extractShareCode(state: String): ShareCode? {
        return state.toUIntOrNull()
    }
}