package de.robolab.client.net.requests

import com.soywiz.klock.DateTime
import de.robolab.client.net.IServerResponse
import de.robolab.common.net.RESTRequestError
import de.robolab.common.net.`throw`
import de.robolab.client.net.contentType
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Result
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

interface IRESTResponse : IServerResponse

typealias RESTResult<T> = Result<T, RESTRequestError>

private const val EAGER_PLANET_EVAL = false

abstract class RESTResponse(protected val parentResponse: IServerResponse) : IRESTResponse {
    final override val status: HttpStatusCode = parentResponse.status
    final override val method: HttpMethod = parentResponse.method
    final override val url: String = parentResponse.url
    final override val body: String? = parentResponse.body
    final override val headers: Map<String, List<String>> = parentResponse.headers
    final override fun <T : Any> parse(deserializer: DeserializationStrategy<T>): T? =
        parentResponse.parse(deserializer)
}

open class ClientPlanetInfoRestResponse(
    serverResponse: IServerResponse,
    triggeringRequest: IRESTRequest<ClientPlanetInfoRestResponse>
) : RESTResponse(serverResponse) {
    val info: PlanetJsonInfo

    init {
        info = if (status != HttpStatusCode.Ok && status != HttpStatusCode.Created) {
            `throw`(triggeringRequest)
        } else when (val mimeType = contentType?.mimeType) {
            MIMEType.JSON -> parse(PlanetJsonInfo.serializer())
                ?: throw IllegalArgumentException("Received empty body for planet-info")
            else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
        }
    }
}

@Suppress("LeakingThis")
open class PlanetResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<PlanetResponse>) :
    RESTResponse(serverResponse) {

    val lines: List<String>
    val lastModified: DateTime

    init {
        if (serverResponse.status != HttpStatusCode.Ok) {
            `throw`(triggeringRequest)
        } else {
            lines = when (val mimeType = serverResponse.typedHeaders.contentTypeHeaders.single().mimeType) {
                MIMEType.JSON -> {
                    parse(ListSerializer(String.serializer())) ?: emptyList()
                }
                MIMEType.PlainText -> {
                    body?.split('\n') ?: emptyList()
                }
                else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
            }
            lastModified = serverResponse.typedHeaders.lastModifiedHeader?.dateTime ?: DateTime.Companion.fromUnix(0)
        }
    }

    val planet: Planet by lazy {
        PlanetFile(lines).planet
    }

    init {
        if (EAGER_PLANET_EVAL)
            planet
    }

}