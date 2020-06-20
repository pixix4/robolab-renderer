package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ID

class PutPlanet(id: ID, content: String? = null) : IRESTRequest<PutPlanet.PutPlanetResponse> {

    constructor(id: ID, planet: PlanetFile) : this(id, planet.content)

    override val method: HttpMethod = HttpMethod.PUT
    override val path: String = "/api/planets/${id.id}"
    override val body: String? = content
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> =
        if (content != null) mapOf(ContentTypeHeader(MIMEType.PlainText))
        else emptyMap()

    override val forceAuth: Boolean = false

    override fun parseResponse(serverResponse: ServerResponse): PutPlanetResponse = PutPlanetResponse(serverResponse)

    class PutPlanetResponse(serverResponse: IServerResponse) : ClientPlanetInfoRestResponse(serverResponse)
}

suspend fun IRobolabServer.putPlanet(id: ID, content: String? = null): PutPlanet.PutPlanetResponse =
    request(PutPlanet(id, content))

suspend fun IRobolabServer.putPlanet(
    id: ID,
    content: String? = null,
    block: RequestBuilder.() -> Unit
): PutPlanet.PutPlanetResponse =
    request(PutPlanet(id, content), block)

suspend fun IRobolabServer.putPlanet(id: ID, planet: PlanetFile): PutPlanet.PutPlanetResponse =
    request(PutPlanet(id, planet))

suspend fun IRobolabServer.putPlanet(
    id: ID,
    planet: PlanetFile,
    block: RequestBuilder.() -> Unit
): PutPlanet.PutPlanetResponse =
    request(PutPlanet(id, planet), block)