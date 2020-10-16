package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object GetMQTTURLs : IUnboundRESTRequest<GetMQTTURLs.MQTTURLsResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/mqtt/urls"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::MQTTURLsResponse)

    class MQTTURLsResponse(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<MQTTURLsResponse>
    ) : RESTResponse(serverResponse) {
        val wssURL: String
        val sslURL: String
        val logURL: String

        operator fun component1() = wssURL
        operator fun component2() = sslURL
        operator fun component3() = logURL

        init {
            if (status != HttpStatusCode.Ok) {
                `throw`(triggeringRequest)
            } else {
                val json = jsonBody!!.jsonObject
                wssURL = json.getValue("wss").jsonPrimitive.content
                sslURL = json.getValue("ssl").jsonPrimitive.content
                logURL = json.getValue("log").jsonPrimitive.content
            }
        }

        override fun toString(): String {
            return "MQTTURLsResponse(wss=\"$wssURL\", ssl=\"$sslURL\", log=\"$logURL\")"
        }
    }
}

suspend fun IRobolabServer.getMQTTURLs() = request(GetMQTTURLs)
suspend fun IRobolabServer.getMQTTURLs(block: RequestBuilder.() -> Unit) = request(GetMQTTURLs, block)