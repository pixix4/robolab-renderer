package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.planet.ClientPlanetInfo
import de.robolab.common.planet.ID
import kotlinx.serialization.builtins.list

object ListPlanets : IRESTRequest<ListPlanets.ListPlanetsResponse> {
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/planets"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = true


    override fun parseResponse(serverResponse: ServerResponse) = ListPlanetsResponse(serverResponse)

    class ListPlanetsResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

        val planets: List<ClientPlanetInfo>

        init {
            planets = if (serverResponse.status != HttpStatusCode.Ok)
                emptyList()
            else when (val mimeType = serverResponse.contentType?.mimeType) {
                MIMEType.JSON -> parse(ClientPlanetInfo.serializer().list)!!
                MIMEType.PlainText -> body!!.split('\n').map(ClientPlanetInfo.Companion::fromPlaintextString)
                else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
            }
        }

        val names: List<String> = planets.map(ClientPlanetInfo::name)
        val ids: List<ID> = planets.map(ClientPlanetInfo::id)
    }

}

suspend fun IRobolabServer.listPlanets(): ListPlanets.ListPlanetsResponse = request(ListPlanets)