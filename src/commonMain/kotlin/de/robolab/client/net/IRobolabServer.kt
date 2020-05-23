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
                            query: List<Pair<String,String>> = emptyList(),
                            headers: List<Pair<String, String>> = emptyList(),
                            forceAuth:Boolean=false): ServerResponse
}