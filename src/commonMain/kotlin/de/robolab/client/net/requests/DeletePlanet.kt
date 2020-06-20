package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.planet.ID

class DeletePlanet(val id: ID) : IRESTRequest<DeletePlanet.DeletePlanetResponse> {
    override val method: HttpMethod = HttpMethod.DELETE
    override val path: String = "/api/planets/${id.id}"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = false

    override fun parseResponse(serverResponse: ServerResponse) = DeletePlanetResponse(serverResponse)

    class DeletePlanetResponse(serverResponse: IServerResponse) : PlanetResponse(serverResponse)
}

suspend fun IRobolabServer.deletePlanet(id: ID): DeletePlanet.DeletePlanetResponse = request(DeletePlanet(id))
suspend fun IRobolabServer.deletePlanet(id: ID, block: RequestBuilder.() -> Unit): DeletePlanet.DeletePlanetResponse =
    request(DeletePlanet(id), block)