package de.robolab.client.net

import de.robolab.common.net.HttpMethod

expect suspend fun sendHttpRequest(
    method: HttpMethod,
    scheme: String,
    host:String,
    port:Int,
    path:String,
    body:String?,
    query: List<Pair<String,String>>,
    headers: List<Pair<String, String>>
) : ServerResponse