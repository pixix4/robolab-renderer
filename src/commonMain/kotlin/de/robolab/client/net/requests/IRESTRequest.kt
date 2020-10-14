package de.robolab.client.net.requests

import de.robolab.client.net.RequestBuilder
import de.robolab.client.net.ServerResponse
import de.robolab.common.net.HttpMethod

interface IRESTRequest<out R> where R : IRESTResponse {
    val requestMethod: HttpMethod
    val requestPath: String
    val requestBody: String?
    val requestQuery: Map<String, String>
    val requestHeader: Map<String, List<String>>

    fun parseResponse(serverResponse: ServerResponse): RESTResult<R>
}

fun RequestBuilder.loadRequest(request: IRESTRequest<*>) {
    method(request.requestMethod)
    path(request.requestPath)
    body(request.requestBody)
    query(request.requestQuery)
    header(request.requestHeader)
}

fun <R> RequestBuilder.buildRequest(baseRequest: IRESTRequest<R>): IRESTRequest<R> where R : IRESTResponse =
    buildRequest(baseRequest::parseResponse)
