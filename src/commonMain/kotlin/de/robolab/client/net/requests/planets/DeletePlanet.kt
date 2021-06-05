package de.robolab.client.net.requests.planets

import de.robolab.client.net.*
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.PlanetResponse
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.planet.utils.ID

class DeletePlanet(val id: ID) : IUnboundRESTRequest<PlanetResponse> {
    override val requestMethod: HttpMethod = HttpMethod.DELETE
    override val requestPath: String = "/api/planet/${id.id}"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::PlanetResponse)
}

suspend fun IRobolabServer.deletePlanet(id: ID) = request(DeletePlanet(id))
suspend fun IRobolabServer.deletePlanet(id: ID, block: RequestBuilder.() -> Unit) = request(DeletePlanet(id), block)