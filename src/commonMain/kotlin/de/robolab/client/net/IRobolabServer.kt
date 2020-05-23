package de.robolab.client.net

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