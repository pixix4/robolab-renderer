package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.*
import de.robolab.common.utils.Version
import de.robolab.common.utils.decode
import kotlinx.serialization.Serializable

object GetVersion : IUnboundRESTRequest<GetVersion.VersionResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/version"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) = parseResponseCatchingWrapper(serverResponse,this,::VersionResponse)

    class VersionResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<VersionResponse>) : RESTResponse(serverResponse) {
        val version: Version

        init {
            if (status != HttpStatusCode.Ok) {
                `throw`(triggeringRequest)
            } else {
                val json = serverResponse.jsonBody!!
                version = VersionWithName.serializer().decode(json).version
            }
        }

        override fun toString(): String {
            return "VersionResponse(version=$version)"
        }
    }

    @Serializable
    class VersionWithName(
        val version: Version,
        val versionName: String
    )
}

suspend fun IRobolabServer.getVersion() = request(GetVersion)
suspend fun IRobolabServer.getVersion(block: RequestBuilder.() -> Unit)= request(GetVersion, block)