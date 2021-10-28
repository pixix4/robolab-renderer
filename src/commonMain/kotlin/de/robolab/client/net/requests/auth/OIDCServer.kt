package de.robolab.client.net.requests.auth

import de.robolab.client.net.RobolabScope
import de.robolab.client.net.URLInfo
import de.robolab.client.net.sendHttpRequest
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.parseOrThrow
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class OIDCServer(val config: OpenIDConfiguration) {

    @Serializable
    data class OpenIDConfiguration(
        @SerialName("authorization_endpoint")
        val authorizationEndpoint: String,
        @SerialName("device_authorization_endpoint")
        val deviceAuthorizationEndpoint: String,
        @SerialName("end_session_endpoint")
        val endSessionEndpoint: String,
        val issuer: String,
        @SerialName("token_endpoint")
        val tokenEndpoint: String,
    )

    suspend fun requestDeviceAuth(
        clientID: String,
        clientSecret: String? = null,
        scope: String = "openid+robolab+offline_access"
    ): DeviceAuthResponse {
        return sendHttpRequest(
            config.deviceAuthorizationEndpoint,
            HttpMethod.POST,
            Parameters.build {
                append("client_id", clientID)
                if (!clientSecret.isNullOrEmpty()) append("client_secret", clientSecret)
                append("scope", scope)
            }.formUrlEncode(),
            mapOf(
                "accept" to listOf(MIMEType.JSON.primaryName),
                "content-type" to listOf(MIMEType.FORM_URLENCODED.primaryName)
            )
        ).parseOrThrow(DeviceAuthResponse.serializer(), ensureStatusCode = null)
    }

    suspend fun pollTokenOnce(
        authResponse: DeviceAuthResponse,
        clientID: String,
        clientSecret: String? = null
    ): TokenResponse = sendHttpRequest(
        config.tokenEndpoint,
        HttpMethod.POST,
        Parameters.build {
            append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            append("client_id", clientID)
            if (!clientSecret.isNullOrEmpty()) append("client_secret", clientSecret)
            append("device_code", authResponse.deviceCode)
        }.formUrlEncode(),
        mapOf(
            "accept" to listOf(MIMEType.JSON.primaryName),
            "content-type" to listOf(MIMEType.FORM_URLENCODED.primaryName)
        ),
        throwOnNonOk = false
    ).parseOrThrow(TokenResponse.serializer(), ensureStatusCode = null)

    suspend fun pollTokenContinuous(
        authResponse: DeviceAuthResponse,
        clientID: String,
        clientSecret: String? = null
    ): TokenResponse {
        var interval: Long = (authResponse.interval ?: 5) * 1000L
        do {
            delay(interval)
            return when (val response: TokenResponse = pollTokenOnce(authResponse, clientID, clientSecret)) {
                is TokenResponse.FinalTokenResponse.AccessDenied -> response
                is TokenResponse.FinalTokenResponse.AccessToken -> response
                is TokenResponse.ExpiredToken -> response
                is TokenResponse.ContinueTokenResponse.AuthorizationPending -> continue
                is TokenResponse.ContinueTokenResponse.SlowDown -> {
                    interval *= 2
                    continue
                }
            }
        } while (true)
    }

    suspend fun performDeviceAuth(
        clientID: String,
        clientSecret: String? = null,
        scope: String = "openid+robolab+offline_access",
        promptHandler: (DeviceAuthPrompt) -> IDeviceAuthPromptCallbacks
    ): TokenResponse.FinalTokenResponse.AccessToken {
        var currentHandler: IDeviceAuthPromptCallbacks? = null
        do {
            val authResp = requestDeviceAuth(clientID, clientSecret, scope)
            if (currentHandler != null)
                currentHandler.onPromptRefresh(authResp.prompt)
            else
                currentHandler = promptHandler(authResp.prompt)
            val pollResult = pollTokenContinuous(authResp, clientID, clientSecret)
            return when (pollResult) {
                is TokenResponse.ExpiredToken -> continue
                is TokenResponse.FinalTokenResponse.AccessDenied -> {
                    currentHandler.onPromptError()
                    throw Exception("Access Denied")
                }
                is TokenResponse.FinalTokenResponse.AccessToken -> {
                    currentHandler.onPromptSuccess()
                    pollResult
                }
                else -> throw IllegalArgumentException("Unexpected token-response: $pollResult")
            }
        } while (true)
    }

    suspend fun performTokenRefresh(
        refreshToken: String,
        clientID: String? = null,
        clientSecret: String? = null,
    ): TokenResponse {
        return sendHttpRequest(
            config.tokenEndpoint,
            HttpMethod.POST,
            Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
                if (!clientID.isNullOrEmpty()) append("client_id", clientID)
                if (!clientSecret.isNullOrEmpty()) append("client_secret", clientSecret)
            }.formUrlEncode(),
            mapOf(
                "accept" to listOf(MIMEType.JSON.primaryName),
                "content-type" to listOf(MIMEType.FORM_URLENCODED.primaryName)
            ),
            throwOnNonOk = false
        ).parseOrThrow(TokenResponse.serializer(), ensureStatusCode = null)
    }

    companion object {
        private val JSON = Json { ignoreUnknownKeys = true }

        suspend fun discover(url: String): OIDCServer {
            val response = (URLInfo.fromURL(url)
                ?: throw IllegalStateException("Could not parse URLInfo for OIDC-discovery: $url")).sendHttpRequest()
            val body = response.jsonBody
                ?: throw IllegalArgumentException("GET for OIDC-Discovery at \"$url\" did not return json")
            val openIDConfiguration: OpenIDConfiguration = JSON.decodeFromJsonElement(body)
            return OIDCServer(openIDConfiguration)
        }

        val RoboLabOIDC: Deferred<OIDCServer?> by lazy {
            RobolabScope.async {
                discover("https://robolab.inf.tu-dresden.de/service/auth/.well-known/openid-configuration")
            }
        }
    }
}
