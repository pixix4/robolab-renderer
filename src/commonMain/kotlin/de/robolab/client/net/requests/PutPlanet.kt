package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ID

class PutPlanet(id: ID, content: String? = null) : IRESTRequest<PutPlanet.PutPlanetResponse> {

    constructor(id: ID, planet: PlanetFile) : this(id, planet.contentString)

    override val requestMethod: HttpMethod = HttpMethod.PUT
    override val requestPath: String = "/api/planets/${id.id}"
    override val requestBody: String? = content
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> =
        if (content != null) mapOf(ContentTypeHeader(MIMEType.PlainText))
        else emptyMap()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::PutPlanetResponse)

    class PutPlanetResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<PutPlanetResponse>) :
        ClientPlanetInfoRestResponse(serverResponse, triggeringRequest)
}

suspend fun IRobolabServer.putPlanet(id: ID, content: String? = null) = request(PutPlanet(id, content))

suspend fun IRobolabServer.putPlanet(
    id: ID,
    content: String? = null,
    block: RequestBuilder.() -> Unit
) = request(PutPlanet(id, content), block)

suspend fun IRobolabServer.putPlanet(id: ID, planet: PlanetFile) = request(PutPlanet(id, planet))

suspend fun IRobolabServer.putPlanet(
    id: ID,
    planet: PlanetFile,
    block: RequestBuilder.() -> Unit
) = request(PutPlanet(id, planet), block)