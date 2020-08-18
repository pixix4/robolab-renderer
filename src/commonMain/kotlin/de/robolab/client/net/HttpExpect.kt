package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.client.statement.request
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.content.TextContent
import io.ktor.util.toMap
import io.ktor.utils.io.charsets.Charsets
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import io.ktor.http.HttpMethod as KtorHttpMethod
import io.ktor.http.HttpStatusCode as KtorHttpStatusCode

expect val client: HttpClient

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
    method: HttpMethod,
    scheme: String,
    host: String,
    port: Int,
    path: String,
    body: String?,
    query: Map<String, String>,
    headers: Map<String, List<String>>
): ServerResponse {
    val builtApplicators = buildApplicators(query, headers)
    @Suppress("REDUNDANT_ELSE_IN_WHEN") val response: HttpResponse = when (method) {
        HttpMethod.GET -> client.get(scheme, host, port, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.PUT -> client.put(scheme, host, port, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.POST -> client.post(scheme, host, port, path, body ?: EmptyContent, builtApplicators)
        HttpMethod.DELETE -> client.delete(scheme, host, port, path, body ?: EmptyContent, builtApplicators)
        else -> client.request {
            this.method = method.toKtorMethod()
            this.host = host
            this.port = port
            this.url {
                this.encodedPath = path
                this.protocol = URLProtocol.createOrDefault(scheme)
                this.host = host
                this.port = port
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
    HttpMethod.GET -> KtorHttpMethod.Get
    HttpMethod.DELETE -> KtorHttpMethod.Delete
    HttpMethod.POST -> KtorHttpMethod.Post
    HttpMethod.PUT -> KtorHttpMethod.Put
    else -> KtorHttpMethod.parse(this.name)
}

fun KtorHttpMethod.toRobolabMethod(): HttpMethod = when (this) {
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
        body = this.readText(fallbackCharset),
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
        val response = sendHttpRequest(method, scheme, host, port, path, body, query, headers)
        callback(response)
    }
}
