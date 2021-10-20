package de.robolab.client.net

import de.robolab.client.net.requests.*
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.robolab.client.net.requests.auth.IDeviceAuthPromptCallbacks
import de.robolab.client.net.requests.auth.TokenResponse
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.headers.AuthorizationHeader
import de.westermann.kobserve.base.ObservableProperty

interface IRobolabServer {
    val hostURL: String
    val hostPort: Int
    val protocol: String
    val authHeaderProperty: ObservableProperty<AuthorizationHeader?>
    var authHeader: AuthorizationHeader?

    fun resetAuthSession()
    suspend fun performDeviceAuth(
        scope: String = "openid+robolab+offline_access",
        promptHandler: (DeviceAuthPrompt) -> IDeviceAuthPromptCallbacks
    ): TokenResponse.FinalTokenResponse.AccessToken

    suspend fun request(
        method: HttpMethod,
        path: String,
        body: String? = null, query: Map<String, String> = emptyMap(),
        headers: Map<String, List<String>> = emptyMap()
    ): ServerResponse
}

suspend fun <R> IRobolabServer.request(request: IUnboundRESTRequest<R>): RESTResult<R> where R : IRESTResponse {
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
    request: IUnboundRESTRequest<R>,
    block: RequestBuilder.() -> Unit
): RESTResult<R> where R : IRESTResponse {
    val builder = RequestBuilder()
    builder.loadRequest(request)
    builder.block()
    return request(builder.buildRequest(request))
}

suspend fun IRobolabServer.ping(path: String): Boolean {
    return pingRemote(
        protocol,
        hostURL,
        hostPort,
        path
    )
}
