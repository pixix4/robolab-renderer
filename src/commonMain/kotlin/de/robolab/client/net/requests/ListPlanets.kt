package de.robolab.client.net.requests

import de.robolab.client.net.*
import de.robolab.common.net.*
import de.robolab.common.planet.ID
import de.robolab.common.utils.filterValuesNotNull
import kotlinx.serialization.builtins.ListSerializer

open class ListPlanets(
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false
) : IUnboundRESTRequest<ListPlanets.ListPlanetsResponse> {
    object Simple : ListPlanets()

    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/planets"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = mapOf(
        "name" to nameExact,
        "nameStartsWith" to nameStartsWith,
        "nameContains" to nameContains,
        "nameEndsWith" to nameEndsWith,
        "ignoreCase" to (if (nameEndsWith ?: nameEndsWith ?: nameContains ?: nameEndsWith != null) {
            if (ignoreCase) "1" else "0"
        } else null)
    ).filterValuesNotNull()
    override val requestHeader: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::ListPlanetsResponse)

    class ListPlanetsResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<ListPlanetsResponse>) :
        RESTResponse(serverResponse) {

        val planets: List<PlanetJsonInfo>

        init {
            planets = if (serverResponse.status != HttpStatusCode.Ok)
                `throw`(triggeringRequest)
            else when (val mimeType = serverResponse.contentType?.mimeType) {
                MIMEType.JSON -> parse(ListSerializer(PlanetJsonInfo.serializer()))
                else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
            } ?: emptyList()
        }

        val names: List<String> = planets.map(PlanetJsonInfo::name)
        val ids: List<ID> = planets.map(PlanetJsonInfo::id)
    }
}

suspend fun IRobolabServer.listPlanets() = request(ListPlanets.Simple)
suspend fun IRobolabServer.listPlanets(block: RequestBuilder.() -> Unit) = request(ListPlanets.Simple, block)
suspend fun IRobolabServer.listPlanets(
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false
) = request(
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
) = request(
    ListPlanets(
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase
    ), block
)