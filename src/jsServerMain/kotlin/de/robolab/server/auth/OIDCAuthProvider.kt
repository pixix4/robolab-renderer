package de.robolab.server.auth

import de.robolab.client.net.RESTAuthSupplier
import de.robolab.client.net.ServerResponse
import de.robolab.client.net.client
import de.robolab.client.net.http
import de.robolab.common.net.BearerToken
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.net.headers.Header
import de.robolab.server.RequestError
import de.robolab.server.jsutils.promise
import de.robolab.server.net.Client
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

open class OIDCIdentityProvider(
    val baseURL: String,
    val redirectURL: String,
    val applicationID: String,
    val applicationSecret: String,
    val normalScopes: String = "openid",
    val adminScopes: String = "openid"
) : IdentityProvider<OIDCIdentityProvider.OIDCUser> {
    override val predicates: Map<String, suspend (OIDCUser, List<String>) -> Boolean> = emptyMap()

    override suspend fun getUsername(user: OIDCUser): String = user.name

    override suspend fun makeIdentity(client: Client, asAdmin: Boolean): OIDCUser {
        val state: String = client.id
        val scopes = if (asAdmin) adminScopes else normalScopes
        val stateHash: String = state.hashCode().toString() //TODO: Improve hashing, probably not secure

        val reconnectInfo = client.oauthRedirect(
            "$baseURL/authorize?" +
                    "client_id=$applicationID&" +
                    "redirect_uri=$redirectURL&" +
                    "response_type=code&" +
                    "state=$stateHash&" +
                    "scope=$scopes"
        )

        val code: String = reconnectInfo.params["code"] ?: throw RequestError(
            HttpStatusCode.BadRequest,
            "'code'-parameter required on reconnectInfo"
        )

        val newStateHash: String = reconnectInfo.params["state"] ?: throw RequestError(
            HttpStatusCode.BadRequest,
            "'state'-parameter required on reconnectInfo"
        )

        if (stateHash != newStateHash) throw RequestError(
            HttpStatusCode.BadRequest,
            "State-Mismatch, you might be victim of a 'CrossSiteRequestForgery'-Attack"
        )

        val tokenResponse = http {
            url("$baseURL/token")
            post()
            query(
                "client_id" to applicationID,
                "client_secret" to applicationSecret,
                "code" to code,
                "grant_type" to "authorization_code",
                "redirect_uri" to redirectURL
            )
        }.exec()
        val token: OIDCTokenInfo = tokenResponse.parse(OIDCTokenInfo.serializer())!!

        val userInfoResponse = http {
            url("$baseURL/userinfo")
            get()
            auth(token)
        }.exec()

        return toUser(token, client, userInfoResponse)
    }

    open suspend fun toUser(token: OIDCTokenInfo, client: Client, userInfo: ServerResponse) = OIDCUser(client, token, "")

    open class OIDCUser internal constructor(val client: Client, private var currentToken: OIDCTokenInfo, val name: String) {
    }

    interface ITokenInfo : RESTAuthSupplier {
        val access_token: String

        @Transient
        override val headers: List<Header>
            get() = listOf(AuthorizationHeader.Bearer(access_token))
    }

    @Serializable
    data class OIDCTokenInfo(
        override val access_token: String,
        val token_type: String = "bearer",
        val expires_in: Int,
        val refresh_token: String? = null
    ) : RESTAuthSupplier, ITokenInfo

}