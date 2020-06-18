package de.robolab.client.net.requests

import com.soywiz.klock.DateTime
import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.planet.ClientPlanetInfo
import de.robolab.common.planet.ID

object ListPlanets : IRESTRequest<ListPlanets.ListPlanetsResponse>{
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/planets"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = true

    private val textResponseRegex: Regex = """^((?:[a-zA-Z0-9_\-]|%3d)+)@([0-9]+):([^\n\r]+)$""".toRegex(RegexOption.MULTILINE)

    override fun parseResponse(serverResponse: ServerResponse) = ListPlanetsResponse(serverResponse)

    class ListPlanetsResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

        val planets: List<ClientPlanetInfo>

        init{
            if(serverResponse.status != HttpStatusCode.Ok){
                planets = emptyList()
            }else{
                when(val mimeType = serverResponse.contentType?.mimeType){
                    MIMEType.JSON -> {
                        planets = jsonBody!!.jsonArray.map{
                            val infoJson = it.jsonObject
                            return@map ClientPlanetInfo(
                                name = infoJson.getPrimitive("name").content,
                                id = ID(infoJson.getPrimitive("id").content),
                                lastModifiedAt = DateTime.Companion.fromUnix(infoJson["lastModified"]!!.longObject)
                            )
                        }
                    }
                    MIMEType.PlainText -> {
                        planets = body!!.split('\n').map {
                            val match = textResponseRegex.matchEntire(it) ?: throw IllegalArgumentException("Cannot parse response \"$it\"")
                            val (idString:String, modifiedAt:String, name:String) = match.destructured
                            return@map ClientPlanetInfo(
                                name = name,
                                id = ID(idString),
                                lastModifiedAt = DateTime.Companion.fromUnix(modifiedAt.toLong())
                            )
                        }
                    }
                    else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
                }
            }
        }

        val names: List<String> = planets.map(ClientPlanetInfo::name)
        val ids: List<ID> = planets.map(ClientPlanetInfo::id)
    }

}

suspend fun IRobolabServer.listPlanets():ListPlanets.ListPlanetsResponse = request(ListPlanets)