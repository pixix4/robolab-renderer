package de.robolab.client.net.requests

import de.robolab.client.net.RequestBuilder
import de.robolab.client.net.ServerResponse
import de.robolab.client.net.URLInfo
import de.robolab.common.net.HttpMethod

interface IRESTRequest<out R> where R : IRESTResponse

interface IUnboundRESTRequest<out R> : IRESTRequest<R> where R : IRESTResponse {
    val requestMethod: HttpMethod
    val requestPath: String
    val requestBody: String?
    val requestQuery: Map<String, String>
    val requestHeader: Map<String, List<String>>

    fun parseResponse(serverResponse: ServerResponse): RESTResult<R>
}

interface IBoundRESTRequest<out R>: IRESTRequest<R> where R: IRESTResponse{
    val requestURL: URLInfo
    val requestHeaders: Map<String,List<String>>
        get() = emptyMap()
    val requestBody: String?
        get() = null
    fun parseResponse(serverResponse: ServerResponse): RESTResult<R>
}

fun RequestBuilder.loadRequest(request: IUnboundRESTRequest<*>) {
    method(request.requestMethod)
    path(request.requestPath)
    body(request.requestBody)
    query(request.requestQuery)
    header(request.requestHeader)
}

fun <R> RequestBuilder.buildRequest(baseRequest: IUnboundRESTRequest<R>): IUnboundRESTRequest<R> where R : IRESTResponse =
    buildRequest(baseRequest::parseResponse)
