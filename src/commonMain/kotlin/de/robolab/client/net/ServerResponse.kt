package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.TypedHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

private val json: Json = Json(JsonConfiguration.Stable)

interface IServerResponse {
    val status: HttpStatusCode
    val method: HttpMethod
    val url:String
    val body:String?
    val headers:Map<String,List<String>>
    val jsonBody: JsonElement?
        get() {
            val body: String? = this.body
            return if (body == null) null else json.parseJson(body)
        }
    val typedHeaders: TypedHeaders
        get() = TypedHeaders.parse(headers)
}

val IServerResponse.contentType: ContentTypeHeader?
    get() = null

data class ServerResponse(
    override val status: HttpStatusCode,
    override val method: HttpMethod,
    override val url:String,
    override val body:String?,
    override val headers:Map<String,List<String>>
) : IServerResponse{
    override val jsonBody: JsonElement? by lazy {return@lazy super.jsonBody}
    override val typedHeaders: TypedHeaders = TypedHeaders.parse(headers)
}