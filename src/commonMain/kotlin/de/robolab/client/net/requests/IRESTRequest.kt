package de.robolab.client.net.requests

import de.robolab.client.net.RequestBuilder
import de.robolab.client.net.ServerResponse
import de.robolab.common.net.HttpMethod

interface IRESTRequest<R> where R : IRESTResponse {
    val method: HttpMethod
    val path: String
    val body: String?
    val query: Map<String, String>
    val headers: Map<String, List<String>>
    val forceAuth: Boolean

    fun parseResponse(serverResponse: ServerResponse): R
}

fun RequestBuilder.loadRequest(request: IRESTRequest<*>) {
    method(request.method)
    path(request.path)
    body(request.body)
    query(request.query)
    header(request.headers)
}

fun <R> RequestBuilder.buildRequest(baseRequest: IRESTRequest<R>): IRESTRequest<R> where R : IRESTResponse =
    buildRequest(baseRequest.forceAuth, baseRequest::parseResponse)
