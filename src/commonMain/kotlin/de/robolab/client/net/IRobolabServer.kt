package de.robolab.client.net

import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IRESTResponse
import de.robolab.common.net.HttpMethod

interface IRobolabServer {
    val hostURL:String
    val hostPort: Int
    val protocol: String
    var credentials: ICredentialProvider?

    fun resetAuthSession()

    suspend fun request(method: HttpMethod,
                        path:String,
                        body:String?=null,
                        query: Map<String, String> = emptyMap(),
                        headers: Map<String, List<String>> = emptyMap(),
                        forceAuth:Boolean=false): ServerResponse
}

suspend fun<R> IRobolabServer.request(request: IRESTRequest<R>) : R where R:IRESTResponse{
    val response = request(request.method,request.path,request.body,request.query,request.headers,request.forceAuth)
    return request.parseResponse(response)
}