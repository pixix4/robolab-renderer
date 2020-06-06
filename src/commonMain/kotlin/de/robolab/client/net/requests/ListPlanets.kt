package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.planet.ID

object ListPlanets : IRESTRequest<ListPlanets.ListPlanetsResponse>{
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/planets"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = true

    override fun parseResponse(serverResponse: ServerResponse) = ListPlanetsResponse(serverResponse)

    class ListPlanetsResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

        val planets: List<PlanetInfo>

        init{
            if(serverResponse.status != HttpStatusCode.Ok){
                planets = emptyList()
            }else{
                when(val mimeType = serverResponse.contentType?.mimeType){
                    MIMEType.JSON -> {
                        planets = jsonBody!!.jsonArray.map{
                            val infoJson = it.jsonObject
                            return@map PlanetInfo(
                                name = infoJson.getPrimitive("name").content,
                                id = ID(infoJson.getPrimitive("id").content)
                            )
                        }
                    }
                    MIMEType.PlainText -> {
                        planets = body!!.split('\n').map {
                            val (idString:String, name:String) = it.split(':',limit=2)
                            return@map PlanetInfo(
                                name = name,
                                id = ID(idString)
                            )
                        }
                    }
                    else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
                }
            }
        }

        val names: List<String> = planets.map(PlanetInfo::name)
        val ids: List<ID> = planets.map(PlanetInfo::id)
    }

    data class PlanetInfo(
        val id:ID,
        val name:String
    )
}

suspend fun IRobolabServer.listPlanets():ListPlanets.ListPlanetsResponse = request(ListPlanets)