package de.robolab.client.net.requests

import com.soywiz.klock.DateTime
import de.robolab.client.net.IServerResponse
import de.robolab.client.net.contentType
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ClientPlanetInfo
import de.robolab.common.planet.Planet
import kotlinx.serialization.DeserializationStrategy

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
    val info: ClientPlanetInfo?

    init {
        info = if (status != HttpStatusCode.Ok && status != HttpStatusCode.Created) {
            null
        } else when (val mimeType = contentType?.mimeType) {
            MIMEType.PlainText -> ClientPlanetInfo.fromPlaintextString(body!!)
            MIMEType.JSON -> parse(ClientPlanetInfo.serializer())
            else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
        }
    }
}
@Suppress("LeakingThis")
open class PlanetResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {

    val planet: Planet?

    val lastModified: DateTime?

    init {
        if (serverResponse.status != HttpStatusCode.Ok) {
            planet = null
            lastModified = null
        } else {
            planet = when (val mimeType = serverResponse.typedHeaders.contentTypeHeaders.single().mimeType) {
                MIMEType.JSON -> {//TODO: ReImplement as Parsing
                    PlanetFile(jsonBody!!.jsonArray.joinToString("\n") { it.primitive.content }).planet
                }
                MIMEType.PlainText -> {
                    PlanetFile(body!!).planet
                }
                else -> throw IllegalArgumentException("Cannot parse MIME-Type '$mimeType'")
            }
            lastModified = serverResponse.typedHeaders.lastModifiedHeader!!.dateTime
        }
    }
}