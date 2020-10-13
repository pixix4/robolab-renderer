package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.planet.ID

class DeletePlanet(val id: ID) : IRESTRequest<DeletePlanet.DeletePlanetResponse> {
    override val method: HttpMethod = HttpMethod.DELETE
    override val path: String = "/api/planets/${id.id}"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) = parseResponseCatchingWrapper(
        serverResponse, this, ::DeletePlanetResponse
    )

    class DeletePlanetResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<DeletePlanetResponse>) :
        PlanetResponse(serverResponse, triggeringRequest)
}

suspend fun IRobolabServer.deletePlanet(id: ID) = request(DeletePlanet(id))
suspend fun IRobolabServer.deletePlanet(id: ID, block: RequestBuilder.() -> Unit) = request(DeletePlanet(id), block)