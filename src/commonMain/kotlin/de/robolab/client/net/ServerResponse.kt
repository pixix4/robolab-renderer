package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.TypedHeaders
import de.robolab.common.net.headers.toLowerCaseKeys
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.*

private val json: Json = Json(JsonConfiguration.Stable)

interface IServerResponse {
    val status: HttpStatusCode
    val method: HttpMethod
    val url: String
    val body: String?
    val headers: Map<String, List<String>>
    val jsonBody: JsonElement?
        get() {
            val body: String? = this.body
            return if (body == null) null else json.parseJson(body)
        }
    val typedHeaders: TypedHeaders
        get() = TypedHeaders.parse(headers)

    fun<T: Any> parse(deserializer: DeserializationStrategy<T>): T? {
        val body: String? = this.body
        return if (body == null) null else json.parse(deserializer, body)
    }
}

val IServerResponse.contentType: ContentTypeHeader?
    get() = typedHeaders.contentTypeHeaders.firstOrNull()

class ServerResponse(
    override val status: HttpStatusCode,
    override val method: HttpMethod,
    override val url: String,
    override val body: String?,
    headers: Map<String, List<String>>
) : IServerResponse {
    override val headers: Map<String, List<String>> = headers.toLowerCaseKeys(true)
    override val jsonBody: JsonElement? by lazy { super.jsonBody }
    override val typedHeaders: TypedHeaders = TypedHeaders.parseLowerCase(headers)
}

val JsonElement.longObject: Long
    get() = when (this) {
        is JsonArray -> (this.getPrimitive(0).int.toLong() and 0xffffffff) or
                ((this.getPrimitive(1).int.toLong() and 0xffffffff).shl(32))
        is JsonObject -> (this.getPrimitive("low_").int.toLong() and 0xffffffff) or
                ((this.getPrimitive("high_").int.toLong() and 0xffffffff).shl(32))
        is JsonPrimitive -> when {
            longOrNull != null -> long
            contentOrNull != null -> content.toLong()
            intOrNull != null -> int.toLong()
            else -> throw IllegalArgumentException("Cannot parse JsonElement to long: $this")
        }
        else -> throw IllegalArgumentException("Cannot parse JsonElement to long: $this")

    }