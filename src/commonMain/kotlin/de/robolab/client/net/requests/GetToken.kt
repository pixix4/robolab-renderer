package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.*
import de.robolab.common.net.headers.AuthorizationHeader
import kotlinx.serialization.Serializable


object GetTokenLinkPair : IRESTRequest<GetTokenLinkPair.TokenLinkResponse> {
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/auth/gitlab/relay"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::TokenLinkResponse)

    class TokenLinkResponse(serverResponse: ServerResponse, triggeringRequest: IRESTRequest<TokenLinkResponse>) :
        RESTResponse(serverResponse), IRESTRequest<TokenLinkResponse.TokenResponse> {
        val tokenPath: String
        val loginPath: String
        override val query: Map<String, String> = emptyMap()


        init {
            if (status != HttpStatusCode.Ok)
                `throw`(triggeringRequest)
            val r = this.parse(R.serializer())
                ?: throw RESTRequestError("Could not parse body", triggeringRequest, this)
            tokenPath = r.token
            loginPath = r.login
        }

        override val path: String = tokenPath

        override fun parseResponse(serverResponse: ServerResponse) =
            parseResponseCatchingWrapper(serverResponse, this, ::TokenResponse)

        @Serializable
        private data class R(
            val token: String,
            val login: String
        )


        class TokenResponse(serverResponse: ServerResponse, triggeringRequest: IRESTRequest<TokenResponse>) :
            RESTResponse(serverResponse) {
            val rawToken: String

            init {
                if (status != HttpStatusCode.Ok)
                    `throw`(triggeringRequest)
                if (contentType?.mimeType != MIMEType.JWT) {
                    throw RESTRequestError(
                        "Cannot parse MIME-Type \"${contentType?.mimeType}\"",
                        triggeringRequest,
                        this
                    )
                }
                rawToken =
                    body ?: throw RESTRequestError("Token response does not have a body", triggeringRequest, this)
            }

            val tokenHeader: AuthorizationHeader.Bearer by lazy {
                AuthorizationHeader.Bearer(rawToken)
            }
        }
    }
}

suspend fun IRobolabServer.getTokenLinkPair() = request(GetTokenLinkPair)
suspend fun IRobolabServer.getTokenLinkPair(block: RequestBuilder.() -> Unit) = request(GetTokenLinkPair, block)
