package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.utils.Version

object GetVersion : IUnboundRESTRequest<GetVersion.VersionResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/version"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::VersionResponse)

    class VersionResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<VersionResponse>) :
        JsonRestResponse<Version.VersionWithName>(
            serverResponse,
            triggeringRequest,
            Version.VersionWithName.serializer()
        ) {
        val version: Version = decodedValue.version

        override fun toString(): String {
            return "VersionResponse(version=$version)"
        }
    }
}

suspend fun IRobolabServer.getVersion() = request(GetVersion)
suspend fun IRobolabServer.getVersion(block: RequestBuilder.() -> Unit) = request(GetVersion, block)
