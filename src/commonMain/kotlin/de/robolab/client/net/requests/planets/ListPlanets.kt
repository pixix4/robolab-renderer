package de.robolab.client.net.requests.planets

import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.JsonRestResponse
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.net.*
import de.robolab.common.planet.utils.ID
import de.robolab.common.utils.filterValuesNotNull
import kotlinx.serialization.builtins.ListSerializer

open class ListPlanets(
    path: String? = null,
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
    liveOnly: Boolean = false,
) : IUnboundRESTRequest<ListPlanets.ListPlanetsResponse> {
    object Flat : ListPlanets(liveOnly = false)
    object Live : ListPlanets(liveOnly = true)

    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = when {
        path == null -> "/api/planets"
        path.startsWith('/') -> "/api/planets$path"
        else -> "/api/planets/$path"
    }
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = mapOf(
        "source" to (if (liveOnly) "live" else "flat"),
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
        JsonRestResponse<List<PlanetJsonInfo>>(
            serverResponse,
            triggeringRequest,
            ListSerializer(PlanetJsonInfo.serializer())
        ) {

        val planets: List<PlanetJsonInfo> = decodedValue
        val names: List<String> = planets.map(PlanetJsonInfo::name)
        val ids: List<ID> = planets.map(PlanetJsonInfo::id)
    }
}

suspend fun IRobolabServer.listPlanets(liveOnly: Boolean = false) =
    request(if (liveOnly) ListPlanets.Live else ListPlanets.Flat)

suspend fun IRobolabServer.listPlanets(
    liveOnly: Boolean = false,
    block: RequestBuilder.() -> Unit
) =
    request(if (liveOnly) ListPlanets.Live else ListPlanets.Flat, block)

suspend fun IRobolabServer.listPlanets(
    path: String? = null,
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
    liveOnly: Boolean = false,
) = request(
    ListPlanets(
        path = path,
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase,
        liveOnly = liveOnly,
    )
)

suspend fun IRobolabServer.listPlanets(
    path: String? = null,
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
    liveOnly: Boolean = false,
    block: RequestBuilder.() -> Unit
) = request(
    ListPlanets(
        path= path,
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase,
        liveOnly = liveOnly,
    ), block
)
