package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode

data class ServerResponse(
    val status: HttpStatusCode,
    val method: HttpMethod,
    val url:String,
    val body:String?,
    val headers:List<Pair<String,String>>
)