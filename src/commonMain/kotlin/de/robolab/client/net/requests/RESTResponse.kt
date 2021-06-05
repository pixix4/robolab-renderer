package de.robolab.client.net.requests

import kotlinx.datetime.Instant
import de.robolab.client.net.IServerResponse
import de.robolab.common.net.*
import de.robolab.common.planet.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Result
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

interface IRESTResponse : IServerResponse

typealias RESTResult<T> = Result<T, RESTRequestException>

private const val EAGER_PLANET_EVAL = false

abstract class RESTResponse(protected val parentResponse: IServerResponse) : IRESTResponse {
    final override val status: HttpStatusCode = parentResponse.status
    final override val method: HttpMethod = parentResponse.method
    final override val url: String = parentResponse.url
    final override val body: String? = parentResponse.body
    final override val headers: Map<String, List<String>> = parentResponse.headers
    final override fun <T : Any> parse(deserializer: DeserializationStrategy<T>): T? =
        parentResponse.parse(deserializer)

    override fun toString(): String {
        return "RESTResponse($method:$url --> ${metaInfoString()}: ${bodyInfoString()})"
    }
}

open class JsonRestResponse<T : Any> : RESTResponse {

    val decodedValue: T

    constructor(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<JsonRestResponse<T>>,
        deserializationStrategy: DeserializationStrategy<T>,
        acceptedStatusCode: HttpStatusCode?
    ) : super(serverResponse) {
        decodedValue = serverResponse.parseOrThrow(deserializationStrategy, triggeringRequest, acceptedStatusCode)
    }

    constructor(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<JsonRestResponse<T>>,
        deserializationStrategy: DeserializationStrategy<T>,
        acceptedStatusCodes: Collection<HttpStatusCode> = HttpStatusCode.okLikeCodes
    ) : super(serverResponse) {
        decodedValue = serverResponse.parseOrThrow(deserializationStrategy, triggeringRequest, acceptedStatusCodes)
    }

    constructor(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<JsonRestResponse<T>>,
        deserializationStrategy: DeserializationStrategy<T>,
        acceptedStatusCode: HttpStatusCode?,
        default: T
    ) : super(serverResponse) {
        decodedValue =
            serverResponse.parseOrThrow(deserializationStrategy, triggeringRequest, acceptedStatusCode, default)
    }

    constructor(
        serverResponse: IServerResponse,
        triggeringRequest: IRESTRequest<JsonRestResponse<T>>,
        deserializationStrategy: DeserializationStrategy<T>,
        acceptedStatusCodes: Collection<HttpStatusCode> = HttpStatusCode.okLikeCodes,
        default: T
    ) : super(serverResponse) {
        decodedValue =
            serverResponse.parseOrThrow(deserializationStrategy, triggeringRequest, acceptedStatusCodes, default)
    }
}

open class ClientPlanetInfoRestResponse(
    serverResponse: IServerResponse,
    triggeringRequest: IRESTRequest<ClientPlanetInfoRestResponse>
) : JsonRestResponse<PlanetJsonInfo>(
    serverResponse,
    triggeringRequest,
    PlanetJsonInfo.serializer(),
    setOf(HttpStatusCode.Ok, HttpStatusCode.Created)
) {
    val info: PlanetJsonInfo = decodedValue
}

@Suppress("LeakingThis")
open class PlanetResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<PlanetResponse>) :
    RESTResponse(serverResponse) {

    val planet: Planet
    val lastModified: Instant

    init {
        serverResponse.requireOk(triggeringRequest)
        serverResponse.requireMimeType(setOf(MIMEType.JSON, MIMEType.PlainText), triggeringRequest)
        planet = serverResponse.format<Planet>(MIMEType.JSON to {
            parse(Planet.serializer()) ?: Planet.EMPTY
        }, MIMEType.PlainText to {
            parse(Planet.serializer()) ?: Planet.EMPTY
        }, triggeringRequest = triggeringRequest)
        lastModified = serverResponse.typedHeaders.lastModifiedHeader?.dateTime ?: Instant.fromEpochMilliseconds(0)
    }
}
