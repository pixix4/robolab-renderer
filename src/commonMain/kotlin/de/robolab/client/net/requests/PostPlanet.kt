package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.parser.PlanetFile

class PostPlanet(content: String? = null) : IRESTRequest<PostPlanet.PostPlanetResponse> {

    constructor(planet: PlanetFile) : this(planet.contentString)

    override val requestMethod: HttpMethod = HttpMethod.POST
    override val requestPath: String = "/api/planets"
    override val requestBody: String? = content
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> =
        if (content != null) mapOf(ContentTypeHeader(MIMEType.PlainText))
        else emptyMap()

    override fun parseResponse(serverResponse: ServerResponse) = parseResponseCatchingWrapper(serverResponse,this,::PostPlanetResponse)

    class PostPlanetResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<PostPlanetResponse>) : ClientPlanetInfoRestResponse(serverResponse, triggeringRequest)
}

suspend fun IRobolabServer.postPlanet(content: String? = null) = request(PostPlanet(content))

suspend fun IRobolabServer.postPlanet(
    content: String? = null,
    block: RequestBuilder.() -> Unit
) = request(PostPlanet(content), block)

suspend fun IRobolabServer.postPlanet(planet: PlanetFile) = request(PostPlanet(planet))

suspend fun IRobolabServer.postPlanet(planet: PlanetFile, block: RequestBuilder.() -> Unit) =
    request(PostPlanet(planet), block)