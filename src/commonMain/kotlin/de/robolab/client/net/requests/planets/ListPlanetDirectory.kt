package de.robolab.client.net.requests.planets

import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.RequestBuilder
import de.robolab.client.net.ServerResponse
import de.robolab.client.net.request
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.JsonRestResponse
import de.robolab.client.net.requests.RESTResult
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.MIMEType
import de.robolab.common.net.data.DirectoryInfo
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.mapOf
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.utils.filterValuesNotNull

open class ListPlanetDirectory(
    path: String? = null,
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
) : IUnboundRESTRequest<JsonRestResponse<DirectoryInfo>> {
    object Simple : ListPlanetDirectory()

    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = when {
        path == null -> "/api/planets"
        path.startsWith('/') -> "/api/planets$path"
        else -> "/api/planets/$path"
    }
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = mapOf(
        "source" to "nested",
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
        parseResponseCatchingWrapper(serverResponse, this) { res, req ->
            JsonRestResponse(
                res,
                req,
                DirectoryInfo.serializer()
            )
        }
}


suspend fun IRobolabServer.listPlanetDirectory() = request(ListPlanetDirectory.Simple)

suspend fun IRobolabServer.listPlanetDirectory(block: RequestBuilder.() -> Unit) =
    request(ListPlanetDirectory.Simple, block)

suspend fun IRobolabServer.listPlanetDirectory(path: String?) = request(ListPlanetDirectory(path))

suspend fun IRobolabServer.listPlanetDirectory(path: String?, block: RequestBuilder.() -> Unit) =
    request(ListPlanetDirectory(path), block)

suspend fun IRobolabServer.listPlanetDirectory(
    path: String? = null,
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
) = request(
    ListPlanetDirectory(
        path = path,
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase,
    )
)

suspend fun IRobolabServer.listPlanetDirectory(
    path: String? = null,
    nameExact: String? = null,
    nameStartsWith: String? = null,
    nameContains: String? = null,
    nameEndsWith: String? = null,
    ignoreCase: Boolean = false,
    block: RequestBuilder.() -> Unit
) = request(
    ListPlanetDirectory(
        path = path,
        nameExact = nameExact,
        nameStartsWith = nameStartsWith,
        nameContains = nameContains,
        nameEndsWith = nameEndsWith,
        ignoreCase = ignoreCase,
    ), block
)
