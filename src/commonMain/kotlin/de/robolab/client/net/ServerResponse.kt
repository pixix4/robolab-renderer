package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.RESTRequestException
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.common.net.headers.TypedHeaders
import de.robolab.common.net.headers.toLowerCaseKeys
import de.robolab.common.utils.RobolabJson
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

interface IServerResponse {
    val status: HttpStatusCode
    val method: HttpMethod
    val url: String
    val body: String?
    val headers: Map<String, List<String>>
    val jsonBody: JsonElement?
        get() {
            val body: String? = this.body
            return if (body == null) null else RobolabJson.parseToJsonElement(body)
        }
    val typedHeaders: TypedHeaders
        get() = TypedHeaders.parse(headers)

    fun <T : Any> parse(deserializer: DeserializationStrategy<T>): T? {
        val body: String? = this.body
        return if (body == null) null else RobolabJson.decodeFromString(deserializer, body)
    }

    fun metaInfoString(sep: String = ":"): String = "$status$sep$mimeType"

    fun bodyInfoString(visibleChars: Int = 6): String =
        body?.let {
            if (visibleChars < 0 || it.length < 2 * visibleChars) '\"' + it + '\"'
            else '\"' + it.substring(0, visibleChars) +
                    "…[${it.length - 2 * visibleChars}+${2 * visibleChars}]…" +
                    it.substring(it.length - visibleChars) + '\"'
        } ?: "null"
}

val IServerResponse.mimeType: MIMEType?
    get() = typedHeaders.contentTypeHeaders.firstOrNull()?.mimeType

private val linkRegex = "<([^<>]+)>(?:\\s*;\\s*((?:[^;,]+(?:;[^;,]+)*)))?(?!\\s*;)".toRegex()

val IServerResponse.links: List<Pair<String, Map<String, String>>>
    get() = headers["link"]?.flatMap {
        linkRegex.findAll(it)
            .map { mr -> (mr.destructured.component1() to mr.destructured.component2()) } // I dont trust the MatchResults to not change while traversing the sequence
    }.orEmpty().map { (link, params) ->
        link to params.split(';').associate { param ->
            if (param.contains("=")) {
                val (paramName, paramValue) = param.split('=', limit = 2)
                paramName.trim() to paramValue.trim().let { value ->
                    if (value.startsWith('"') && value.endsWith('"')) value.substring(1, value.length - 1)
                    else value
                }
            } else {
                param.trim() to "true"
            }
        }
    }

val IServerResponse.linksRelMap: Map<String, String>
    get() = links.filter { it.second.containsKey("rel") }.associate { (link, params) -> params.getValue("rel") to link }

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

    override fun toString(): String {
        return "ServerResponse($method:$url --> ${metaInfoString()}: ${bodyInfoString()})"
    }
}