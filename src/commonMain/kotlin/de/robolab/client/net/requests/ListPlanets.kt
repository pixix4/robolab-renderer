package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.planet.ID
import de.robolab.common.utils.filterValuesNotNull
import kotlinx.serialization.builtins.ListSerializer

open class ListPlanets(
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false
) : IRESTRequest<ListPlanets.ListPlanetsResponse> {
    object Simple : ListPlanets()

    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/planets"
    override val body: String? = null
    override val query: Map<String, String> = mapOf(
        "name" to nameExact,
        "nameStartsWith" to nameStartsWith,
        "nameContains" to nameContains,
        "nameEndsWith" to nameEndsWith,
        "ignoreCase" to (if (nameEndsWith ?: nameEndsWith ?: nameContains ?: nameEndsWith != null) {
            if (ignoreCase) "1" else "0"
        } else null)
    ).filterValuesNotNull()
    override val headers: Map<String, List<String>> = mapOf()
    override val forceAuth: Boolean = true

    override fun parseResponse(serverResponse: ServerResponse) = ListPlanetsResponse(serverResponse)

    class ListPlanetsResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

        val planets: List<PlanetJsonInfo>

        init {
            planets = if (serverResponse.status != HttpStatusCode.Ok)
                emptyList()
            else when (val mimeType = serverResponse.contentType?.mimeType) {
                MIMEType.JSON -> parse(ListSerializer(PlanetJsonInfo.serializer()))
                else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
            } ?: emptyList()
        }

        val names: List<String> = planets.map(PlanetJsonInfo::name)
        val ids: List<ID> = planets.map(PlanetJsonInfo::id)
    }
}

suspend fun IRobolabServer.listPlanets(): ListPlanets.ListPlanetsResponse = request(ListPlanets.Simple)
suspend fun IRobolabServer.listPlanets(block: RequestBuilder.() -> Unit) = request(ListPlanets.Simple, block)
suspend fun IRobolabServer.listPlanets(
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false
): ListPlanets.ListPlanetsResponse = request(
    ListPlanets(
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase
    )
)
suspend fun IRobolabServer.listPlanets(
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
    block: RequestBuilder.() -> Unit
): ListPlanets.ListPlanetsResponse = request(
    ListPlanets(
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase
    ), block
)