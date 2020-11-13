package de.robolab.client.net.requests.auth

import de.robolab.client.net.*
import de.robolab.client.net.requests.*
import de.robolab.common.net.*
import de.robolab.common.net.headers.AuthorizationHeader
import kotlinx.serialization.Serializable


@Serializable
data class TokenLinkPair(
    val token: String,
    val login: String
)

object GetTokenLinkPair : IUnboundRESTRequest<GetTokenLinkPair.TokenLinkResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/auth/gitlab/relay"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, GetTokenLinkPair::TokenLinkResponse)

    class TokenLinkResponse(serverResponse: ServerResponse, triggeringRequest: IRESTRequest<TokenLinkResponse>) :
        JsonRestResponse<TokenLinkPair>(
            serverResponse,
            triggeringRequest,
            TokenLinkPair.serializer()
        ), IBoundRestRequest<TokenLinkResponse.TokenResponse> {
        val tokenURL: String = decodedValue.token
        val loginURL: String = decodedValue.login
        override val requestURL: URLInfo = (URLInfo.fromURL(tokenURL) ?: throw RESTRequestException(
            "Could not parse URL $tokenURL",
            triggeringRequest,
            serverResponse
        ))

        override fun parseResponse(serverResponse: ServerResponse) =
            parseResponseCatchingWrapper(serverResponse, this, TokenLinkResponse::TokenResponse)

        class TokenResponse(serverResponse: ServerResponse, triggeringRequest: IRESTRequest<TokenResponse>) :
            RESTResponse(serverResponse) {
            val rawToken: String

            init {
                serverResponse.requireOk(triggeringRequest)
                serverResponse.requireMimeType(MIMEType.JWT, triggeringRequest)
                rawToken = serverResponse.requireBody(triggeringRequest)
            }

            val tokenHeader: AuthorizationHeader.Bearer by lazy {
                AuthorizationHeader.Bearer(rawToken)
            }
        }
    }
}

suspend fun IRobolabServer.getTokenLinkPair() = request(GetTokenLinkPair)
suspend fun IRobolabServer.getTokenLinkPair(block: RequestBuilder.() -> Unit) = request(GetTokenLinkPair, block)
