package de.robolab.client.net.requests

import de.robolab.client.net.ServerResponse
import de.robolab.common.net.HttpMethod

interface IRESTRequest<R> where R:IRESTResponse {
    val method: HttpMethod
    val path:String
    val body:String?
    val query:Map<String,String>
    val headers:Map<String,List<String>>
    val forceAuth:Boolean

    fun parseResponse(serverResponse: ServerResponse): R
}