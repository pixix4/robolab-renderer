package de.robolab.client.net.requests

import com.soywiz.klock.DateTime
import de.robolab.client.net.IServerResponse
import de.robolab.client.net.contentType
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Planet
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

interface IRESTResponse : IServerResponse

abstract class RESTResponse(protected val parentResponse: IServerResponse) : IRESTResponse {
    final override val status: HttpStatusCode = parentResponse.status
    final override val method: HttpMethod = parentResponse.method
    final override val url: String = parentResponse.url
    final override val body: String? = parentResponse.body
    final override val headers: Map<String, List<String>> = parentResponse.headers
    final override fun <T : Any> parse(deserializer: DeserializationStrategy<T>): T? =
        parentResponse.parse(deserializer)
}

open class ClientPlanetInfoRestResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {
    val info: PlanetJsonInfo?

    init {
        info = if (status != HttpStatusCode.Ok && status != HttpStatusCode.Created) {
            null
        } else when (val mimeType = contentType?.mimeType) {
            MIMEType.PlainText -> PlanetJsonInfo.fromPlaintextString(body ?: "")
            MIMEType.JSON -> parse(PlanetJsonInfo.serializer())
            else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
        }
    }
}
@Suppress("LeakingThis")
open class PlanetResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

    val lines: List<String>
    val lastModified: DateTime

    init {
        if (serverResponse.status != HttpStatusCode.Ok) {
            lines = emptyList()
            lastModified = DateTime.Companion.fromUnix(0)
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
}