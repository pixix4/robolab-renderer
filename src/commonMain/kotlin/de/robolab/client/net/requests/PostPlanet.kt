package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.parser.PlanetFile

class PostPlanet(content: String? = null) : IRESTRequest<PostPlanet.PostPlanetResponse> {

    constructor(planet: PlanetFile) : this(planet.contentString)

    override val method: HttpMethod = HttpMethod.POST
    override val path: String = "/api/planets"
    override val body: String? = content
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> =
        if (content != null) mapOf(ContentTypeHeader(MIMEType.PlainText))
        else emptyMap()

    override val forceAuth: Boolean = false

    override fun parseResponse(serverResponse: ServerResponse): PostPlanetResponse = PostPlanetResponse(serverResponse)

    class PostPlanetResponse(serverResponse: IServerResponse) : ClientPlanetInfoRestResponse(serverResponse)
}

suspend fun IRobolabServer.postPlanet(content: String? = null): PostPlanet.PostPlanetResponse =
    request(PostPlanet(content))

suspend fun IRobolabServer.postPlanet(
    content: String? = null,
    block: RequestBuilder.() -> Unit
): PostPlanet.PostPlanetResponse =
    request(PostPlanet(content), block)

suspend fun IRobolabServer.postPlanet(planet: PlanetFile): PostPlanet.PostPlanetResponse =
    request(PostPlanet(planet))

suspend fun IRobolabServer.postPlanet(
    planet: PlanetFile,
    block: RequestBuilder.() -> Unit
): PostPlanet.PostPlanetResponse =
    request(PostPlanet(planet), block)