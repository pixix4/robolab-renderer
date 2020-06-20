package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.planet.ID

class GetPlanet(val id: ID) : IRESTRequest<GetPlanet.GetPlanetResponse> {
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/planets/${id.id}"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = false

    override fun parseResponse(serverResponse: ServerResponse) = GetPlanetResponse(serverResponse)

    class GetPlanetResponse(serverResponse: IServerResponse) : PlanetResponse(serverResponse)
}

suspend fun IRobolabServer.getPlanet(id: ID): GetPlanet.GetPlanetResponse = request(GetPlanet(id))
suspend fun IRobolabServer.getPlanet(id: ID, block: RequestBuilder.() -> Unit): GetPlanet.GetPlanetResponse =
    request(GetPlanet(id), block)