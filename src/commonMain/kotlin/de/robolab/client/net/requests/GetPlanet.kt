package de.robolab.client.net.requests

import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.IServerResponse
import de.robolab.client.net.ServerResponse
import de.robolab.client.net.request
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ID
import de.robolab.common.planet.Planet

class GetPlanet(val id:ID) : IRESTRequest<GetPlanet.GetPlanetResponse>{
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/planets/${id.id}"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = false

    override fun parseResponse(serverResponse: ServerResponse) = GetPlanetResponse(serverResponse)

    class GetPlanetResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

        val planet: Planet?

        init{
            planet = if(serverResponse.status != HttpStatusCode.Ok){
                null
            }else{
                when(val mimeType = serverResponse.typedHeaders.contentTypeHeaders.single().mimeType){
                    MIMEType.JSON -> {
                        PlanetFile(jsonBody!!.jsonArray.joinToString("\n")).planet
                    }
                    MIMEType.PlainText -> {
                        PlanetFile(body!!).planet
                    }
                    else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
                }
            }
        }
    }
}

suspend fun IRobolabServer.getPlanet(id:ID):GetPlanet.GetPlanetResponse = request(GetPlanet(id))