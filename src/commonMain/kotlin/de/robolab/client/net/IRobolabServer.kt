package de.robolab.client.net

import de.robolab.client.net.requests.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.headers.AuthorizationHeader

interface IRobolabServer {
    val hostURL: String
    val hostPort: Int
    val protocol: String
    var authHeader: AuthorizationHeader?

    fun resetAuthSession()

    suspend fun request(
        method: HttpMethod,
        path: String,
        body: String? = null, query: Map<String, String> = emptyMap(),
        headers: Map<String, List<String>> = emptyMap()
    ): ServerResponse
}

suspend fun <R> IRobolabServer.request(request: IRESTRequest<R>): RESTResult<R> where R : IRESTResponse {
    val response = request(
        request.requestMethod,
        request.requestPath,
        request.requestBody,
        request.requestQuery,
        request.requestHeader
    )
    return request.parseResponse(response)
}

suspend fun <R> IRobolabServer.request(
    request: IRESTRequest<R>,
    block: RequestBuilder.() -> Unit
): RESTResult<R> where R : IRESTResponse {
    val builder = RequestBuilder()
    builder.loadRequest(request)
    builder.block()
    return request(builder.buildRequest(request))
}