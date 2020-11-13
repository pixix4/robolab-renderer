package de.robolab.client.net.requests.mqtt

import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.JsonRestResponse
import de.robolab.common.net.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MQTTTopicsInfo(
    @SerialName("subscribe")
    val subscribeTopicPatterns: List<String>,
    @SerialName("publish")
    val publishTopicPatterns: List<String>
)

open class GetMQTTTopics(username: String? = null) :
    IUnboundRESTRequest<GetMQTTTopics.MQTTTopicsResponse> {
    object Simple : GetMQTTTopics()

    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/mqtt/topics"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = if (username == null) emptyMap() else mapOf("username" to username)
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, GetMQTTTopics::MQTTTopicsResponse)


    class MQTTTopicsResponse(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<MQTTTopicsResponse>
    ) : JsonRestResponse<MQTTTopicsInfo>(serverResponse, triggeringRequest, MQTTTopicsInfo.serializer()) {
        val subscribeTopicPatterns = decodedValue.subscribeTopicPatterns
        val publishTopicPatterns = decodedValue.publishTopicPatterns

        override fun toString(): String {
            return "MQTTTopicsResponse($decodedValue)"
        }
    }
}

suspend fun IRobolabServer.getMQTTTopics() = request(GetMQTTTopics.Simple)
suspend fun IRobolabServer.getMQTTTopics(block: RequestBuilder.() -> Unit) = request(GetMQTTTopics.Simple, block)
suspend fun IRobolabServer.getMQTTTopics(username: String?) = request(GetMQTTTopics(username))
suspend fun IRobolabServer.getMQTTTopics(username: String?, block: RequestBuilder.() -> Unit) = request(GetMQTTTopics(username), block)