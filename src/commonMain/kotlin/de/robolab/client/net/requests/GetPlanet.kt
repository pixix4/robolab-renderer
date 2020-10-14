package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.planet.ID

class GetPlanet(val id: ID) : IUnboundRESTRequest<GetPlanet.GetPlanetResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/planets/${id.id}"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::GetPlanetResponse)

    class GetPlanetResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<GetPlanetResponse>) :
        PlanetResponse(serverResponse, triggeringRequest)
}

suspend fun IRobolabServer.getPlanet(id: ID) = request(GetPlanet(id))
suspend fun IRobolabServer.getPlanet(id: ID, block: RequestBuilder.() -> Unit) = request(GetPlanet(id), block)