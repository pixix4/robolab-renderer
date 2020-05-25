package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.client.statement.request
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpStatusCode as KtorHttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.util.flattenEntries
import io.ktor.util.toMap
import io.ktor.utils.io.charsets.Charsets
import io.ktor.http.HttpMethod as KtorHttpMethod

expect val client: HttpClient

suspend fun sendHttpRequest(
    method: HttpMethod,
    scheme: String,
    host:String,
    port:Int,
    path:String,
    body:String?,
    query: Map<String,String>,
    headers: Map<String, List<String>>
): ServerResponse {
    val builtApplicators = buildApplicators(query, headers)
    val response: HttpResponse=when(method){
        HttpMethod.GET -> client.get(scheme, host, port, path,body?:EmptyContent, builtApplicators)
        HttpMethod.PUT -> client.put(scheme, host, port, path, body?:EmptyContent, builtApplicators)
        HttpMethod.POST -> client.post(scheme, host, port, path, body?:EmptyContent, builtApplicators)
        HttpMethod.DELETE -> client.delete(scheme, host, port, path, body?:EmptyContent, builtApplicators)
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
            if(body!=null)
                this.body = body
            builtApplicators(this)
        }
    }
    return response.toRobolabResponse()
}

private fun buildApplicators(query: Map<String,String>, headers: Map<String, List<String>>) : (HttpRequestBuilder.()->Unit) = {
    query.forEach { this.url.parameters.appendMissing(it.key, listOf(it.value))}
    headers.forEach { this.headers.appendMissing(it.key, it.value) }
}

fun HttpMethod.toKtorMethod():KtorHttpMethod = when(this){
    HttpMethod.GET -> KtorHttpMethod.Get
    HttpMethod.DELETE -> KtorHttpMethod.Delete
    HttpMethod.POST -> KtorHttpMethod.Post
    HttpMethod.PUT -> KtorHttpMethod.Put
    else -> KtorHttpMethod.parse(this.name)
}

fun KtorHttpMethod.toRobolabMethod():HttpMethod = when(this){
    KtorHttpMethod.Get -> HttpMethod.GET
    KtorHttpMethod.Delete -> HttpMethod.DELETE
    KtorHttpMethod.Post -> HttpMethod.POST
    KtorHttpMethod.Put -> HttpMethod.PUT
    else -> throw IllegalArgumentException("Unknown http-method: $this")
}

fun KtorHttpStatusCode.toRobolabStatusCode(): HttpStatusCode =
    HttpStatusCode.get(this.value)?: throw IllegalArgumentException("Unknown http-status-code: $this")

private val fallbackCharset = Charsets.UTF_8

suspend fun HttpResponse.toRobolabResponse(): ServerResponse =
    ServerResponse(
        status = this.status.toRobolabStatusCode(),
        method = this.request.method.toRobolabMethod(),
        url = this.request.url.encodedPath,
        body = this.readText(fallbackCharset),
        headers = this.headers.toMap()
    )