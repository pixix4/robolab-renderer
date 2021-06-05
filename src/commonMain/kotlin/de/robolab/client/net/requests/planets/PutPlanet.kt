package de.robolab.client.net.requests.planets

import de.robolab.client.net.*
import de.robolab.client.net.requests.ClientPlanetInfoRestResponse
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetFile
import de.robolab.common.planet.utils.ID

class PutPlanet(id: ID, content: Planet?) : IUnboundRESTRequest<ClientPlanetInfoRestResponse> {

    override val requestMethod: HttpMethod = HttpMethod.PUT
    override val requestPath: String = "/api/planet/${id.id}"
    override val requestBody: String? = content?.let { PlanetFile.stringify(it) }
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> =
        if (content != null) mapOf(ContentTypeHeader(MIMEType.PlainText))
        else emptyMap()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::ClientPlanetInfoRestResponse)
}

suspend fun IRobolabServer.putPlanet(id: ID, content: Planet? = null) = request(PutPlanet(id, content))
