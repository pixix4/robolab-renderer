package de.robolab.client.net.requests.planets

import de.robolab.client.net.*
import de.robolab.client.net.requests.ClientPlanetInfoRestResponse
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.parser.PlanetFile

class PostPlanet(content: String? = null, path: String? = null) :
    IUnboundRESTRequest<ClientPlanetInfoRestResponse> {

    constructor(planet: PlanetFile, path: String? = null) : this(planet.contentString, path)

    override val requestMethod: HttpMethod = HttpMethod.POST
    override val requestPath: String = when {
        path == null -> "/api/planet"
        path.startsWith('/') -> "/api/planets$path"
        else -> "/api/planets/$path"
    }
    override val requestBody: String? = content
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> =
        if (content != null) mapOf(ContentTypeHeader(MIMEType.PlainText))
        else emptyMap()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::ClientPlanetInfoRestResponse)
}

suspend fun IRobolabServer.postPlanet(content: String? = null, path: String? = null) =
    request(PostPlanet(content, path))

suspend fun IRobolabServer.postPlanet(
    content: String? = null,
    path: String? = null,
    block: RequestBuilder.() -> Unit
) = request(PostPlanet(content, path), block)

suspend fun IRobolabServer.postPlanet(planet: PlanetFile, path: String? = null) = request(PostPlanet(planet, path))

suspend fun IRobolabServer.postPlanet(planet: PlanetFile, path: String? = null, block: RequestBuilder.() -> Unit) =
    request(PostPlanet(planet, path), block)
