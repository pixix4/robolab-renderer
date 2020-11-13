package de.robolab.client.net.requests.mqtt

import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.JsonRestResponse
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.parseResponseCatchingWrapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MQTTConnectionInfo(
    override val username: String,
    override val password: String,
    @SerialName("subscribe")
    val subscribeTopicPatterns: List<String>,
    @SerialName("publish")
    val publishTopicPatterns: List<String>
) : ICredentialProvider {
    override fun toString(): String {
        return "MQTTConnectionInfo(username=\"$username\",password=\"${
            password.substring(
                0,
                3
            ) + "*".repeat(password.length - 3)
        }\",subscribeTopics=$subscribeTopicPatterns,publishTopics=$publishTopicPatterns)"
    }
}

open class GetMQTTConnection(username: String? = null) :
    IUnboundRESTRequest<GetMQTTConnection.MQTTConnectionResponse> {
    object Simple : GetMQTTConnection()

    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/mqtt/connection"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = if (username == null) emptyMap() else mapOf("username" to username)
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, GetMQTTConnection::MQTTConnectionResponse)


    class MQTTConnectionResponse(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<MQTTConnectionResponse>
    ) : JsonRestResponse<MQTTConnectionInfo>(serverResponse, triggeringRequest, MQTTConnectionInfo.serializer()),
        ICredentialProvider {
        val subscribeTopicPatterns = decodedValue.subscribeTopicPatterns
        val publishTopicPatterns = decodedValue.publishTopicPatterns
        override val username = decodedValue.username
        override val password = decodedValue.password

        override fun toString(): String {
            return "MQTTConnectionResponse($decodedValue)"
        }
    }
}

suspend fun IRobolabServer.getMQTTConnection() = request(GetMQTTConnection.Simple)
suspend fun IRobolabServer.getMQTTConnection(block: RequestBuilder.() -> Unit) =
    request(GetMQTTConnection.Simple, block)

suspend fun IRobolabServer.getMQTTConnection(username: String?) = request(GetMQTTConnection(username))
suspend fun IRobolabServer.getMQTTConnection(username: String?, block: RequestBuilder.() -> Unit) =
    request(GetMQTTConnection(username), block)