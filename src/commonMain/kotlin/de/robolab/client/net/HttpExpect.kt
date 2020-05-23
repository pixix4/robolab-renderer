package de.robolab.client.net

import de.robolab.common.net.HttpMethod

expect suspend fun sendHttpRequest(
    method: HttpMethod,
    scheme: String,
    host:String,
    port:Int,
    path:String,
    body:String?,
    query: Map<String, String>,
    headers: Map<String, List<String>>
) : ServerResponse