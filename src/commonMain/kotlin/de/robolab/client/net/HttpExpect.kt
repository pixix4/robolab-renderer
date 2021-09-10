package de.robolab.client.net

import de.robolab.client.net.requests.IBoundRESTRequest
import de.robolab.client.net.requests.IRESTResponse
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName
import io.ktor.http.HttpMethod as KtorHttpMethod
import io.ktor.http.HttpStatusCode as KtorHttpStatusCode

expect val client: HttpClient

expect suspend fun pingRemote(
    scheme: String,
    host: String,
    port: Int,
    path: String,
): Boolean

//some headers are blacklisted by the engine and require custom applicators
private val specialHeaderApplicators: Map<String, (HttpRequestBuilder, List<String>) -> Unit> =
    mapOf(
        "content-type" to { builder, values ->
            val value = values.singleOrNull() ?: return@to
            val body = builder.body
            builder.body = TextContent(body.toString(), ContentType.parse(value))
        }
    )

suspend fun sendHttpRequest(
    url: String,
    method: HttpMethod = HttpMethod.GET,
    body: String? = null,
    headers: Map<String, List<String>> = emptyMap()
): ServerResponse = sendHttpRequest(
    URLInfo.fromURL(url, method)
        ?: throw IllegalArgumentException("Invalid URL: $url"), body, headers
)

suspend fun <R : IRESTResponse> sendHttpRequest(boundRequest: IBoundRESTRequest<R>) =
    boundRequest.parseResponse(
        sendHttpRequest(
            boundRequest.requestURL,
            boundRequest.requestBody,
            boundRequest.requestHeaders
        )
    )

@JvmName("sendHttpRequestReceiving")
suspend fun <R : IRESTResponse> IBoundRESTRequest<R>.sendHttpRequest() = sendHttpRequest(this)

suspend fun sendHttpRequest(
    urlInfo: URLInfo,
    body: String? = null,
    headers: Map<String, List<String>> = emptyMap()
): ServerResponse =
    sendHttpRequest(
        urlInfo.method,
        urlInfo.protocol,
        urlInfo.host,
        urlInfo.port,
        urlInfo.path,
        body,
        urlInfo.query,
        headers
    )

@JvmName("sendHttpRequestReceiving")
suspend fun URLInfo.sendHttpRequest(body: String? = null, headers: Map<String, List<String>> = emptyMap()) =
    sendHttpRequest(this, body, headers)

suspend fun sendHttpRequest(
    method: HttpMethod,
    scheme: String,
    host: String,
    port: Int,
    path: String,
    body: String?,
    query: Map<String, String>,
    headers: Map<String, List<String>>,
    throwOnNonOk: Boolean = true
): ServerResponse {
    if (!throwOnNonOk) {
        return try {
            sendHttpRequest(
                method = method,
                scheme = scheme,
                host = host,
                port = port,
                path = path,
                body = body,
                query = query,
                headers = headers,
                throwOnNonOk = true
            )
        } catch (ex: ClientRequestException) {
            ex.response.toRobolabResponse()
        }
    }

    val p = if (port == 443 && scheme.startsWith("https") || port == 80 && scheme.startsWith("http")) 0 else port

    val builtApplicators = buildApplicators(query, headers)
    @Suppress("REDUNDANT_ELSE_IN_WHEN") val response: HttpResponse = when (method) {
        HttpMethod.HEAD -> client.head(scheme, host, p, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.GET -> client.get(scheme, host, p, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.PUT -> client.put(scheme, host, p, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.POST -> client.post(scheme, host, p, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.DELETE -> client.delete(scheme, host, p, path, body ?: EmptyContent, builtApplicators)
        else -> client.request {
            this.method = method.toKtorMethod()
            this.host = host
            this.port = p
            this.url {
                this.encodedPath = path
                this.protocol = URLProtocol.createOrDefault(scheme)
                this.host = host

                this.port = if (port == 443 && scheme.startsWith("https") || port == 80 && scheme.startsWith("http")) 0 else port
            }

            if (body != null)
                this.body = body
            builtApplicators(this)
        }
    }
    return response.toRobolabResponse()
}

private fun buildApplicators(
    query: Map<String, String>,
    headers: Map<String, List<String>>
): (HttpRequestBuilder.() -> Unit) = {
    query.forEach { this.url.parameters.appendMissing(it.key, listOf(it.value)) }
    val (specialHeaders, normalHeaders) = headers.asIterable()
        .partition { specialHeaderApplicators.containsKey(it.key) }
    specialHeaders.forEach {
        specialHeaderApplicators[it.key]!!(this, it.value)
    }
    normalHeaders.forEach {
        this.headers.appendMissing(it.key, it.value)
    }
}

@Suppress("REDUNDANT_ELSE_IN_WHEN")
fun HttpMethod.toKtorMethod(): KtorHttpMethod = when (this) {
    HttpMethod.HEAD -> KtorHttpMethod.Head
    HttpMethod.GET -> KtorHttpMethod.Get
    HttpMethod.DELETE -> KtorHttpMethod.Delete
    HttpMethod.POST -> KtorHttpMethod.Post
    HttpMethod.PUT -> KtorHttpMethod.Put
    else -> KtorHttpMethod.parse(this.name)
}

fun KtorHttpMethod.toRobolabMethod(): HttpMethod = when (this) {
    KtorHttpMethod.Head -> HttpMethod.HEAD
    KtorHttpMethod.Get -> HttpMethod.GET
    KtorHttpMethod.Delete -> HttpMethod.DELETE
    KtorHttpMethod.Post -> HttpMethod.POST
    KtorHttpMethod.Put -> HttpMethod.PUT
    else -> throw IllegalArgumentException("Unknown http-method: $this")
}

fun KtorHttpStatusCode.toRobolabStatusCode(): HttpStatusCode =
    HttpStatusCode.get(this.value) ?: throw IllegalArgumentException("Unknown http-status-code: $this")

private val fallbackCharset = Charsets.UTF_8

suspend fun HttpResponse.toRobolabResponse(): ServerResponse =
    ServerResponse(
        status = this.status.toRobolabStatusCode(),
        method = this.request.method.toRobolabMethod(),
        url = this.request.url.encodedPath,
        body = try {
            this.readText(fallbackCharset)
        } catch (e: Exception) {
            null
        },
        headers = this.headers.toMap()
    )


val RobolabScope = MainScope()
fun sendHttpRequest(
    method: HttpMethod,
    scheme: String,
    host: String,
    port: Int,
    path: String,
    body: String?,
    query: Map<String, String>,
    headers: Map<String, List<String>>,
    callback: (ServerResponse) -> Unit
) {
    RobolabScope.launch {
        val response = sendHttpRequest(method, scheme, host, port, path, body, query, headers, throwOnNonOk = false)
        callback(response)
    }
}
