package de.robolab.client.net.requests.mqtt


import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.RESTResponse
import de.robolab.common.net.*
import de.robolab.common.net.headers.AuthorizationHeader

open class GetMQTTCredentials(username: String? = null) :
    IUnboundRESTRequest<GetMQTTCredentials.MQTTCredentialsResponse> {
    object Simple : GetMQTTCredentials()

    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/mqtt/credentials"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = if (username == null) emptyMap() else mapOf("username" to username)
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.PlainText.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, GetMQTTCredentials::MQTTCredentialsResponse)


    class MQTTCredentialsResponse(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<MQTTCredentialsResponse>
    ) : RESTResponse(serverResponse) {
        val credentials: ICredentialProvider

        init {
            serverResponse.requireOk(triggeringRequest)
            val (username, password) = serverResponse.requireBody().split(':', limit = 2)
            credentials = AuthorizationHeader.Basic(username, password)
        }

        override fun toString(): String {
            return "MQTTCredentialsResponse(username=\"${credentials.username}\",password=\"${
                credentials.password.substring(
                    0,
                    3
                ) + "*".repeat(credentials.password.length - 3)
            }\")"
        }
    }
}

suspend fun IRobolabServer.getMQTTCredentials() = request(GetMQTTCredentials.Simple)
suspend fun IRobolabServer.getMQTTCredentials(block: RequestBuilder.() -> Unit) = request(GetMQTTCredentials.Simple, block)
suspend fun IRobolabServer.getMQTTCredentials(username: String?) = request(GetMQTTCredentials(username))
suspend fun IRobolabServer.getMQTTCredentials(username: String?, block: RequestBuilder.() -> Unit) = request(GetMQTTCredentials(username), block)