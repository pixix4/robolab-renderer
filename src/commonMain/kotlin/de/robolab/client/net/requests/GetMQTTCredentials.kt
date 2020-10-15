package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.*
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.utils.decode
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object GetMQTTCredentials : IUnboundRESTRequest<GetMQTTCredentials.MQTTCredentialsResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/info/exam"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.PlainText.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::MQTTCredentialsResponse)

    class MQTTCredentialsResponse(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<MQTTCredentialsResponse>
    ) : RESTResponse(serverResponse) {
        val credentials: ICredentialProvider

        init {
            if (status != HttpStatusCode.Ok) {
                `throw`(triggeringRequest)
            } else {
                val (username, password) = serverResponse.body!!.split(':',limit=2)
                credentials = AuthorizationHeader.Basic(username, password)
            }
        }

        override fun toString(): String {
            return "MQTTCredentialsResponse(username=${credentials.username},password=${
                credentials.password.substring(
                    0,
                    3
                ) + "*".repeat(credentials.password.length - 3)
            })"
        }
    }
}

suspend fun IRobolabServer.getMQTTCredentials() = request(GetMQTTCredentials)
suspend fun IRobolabServer.getMQTTCredentials(block: RequestBuilder.() -> Unit) = request(GetMQTTCredentials, block)