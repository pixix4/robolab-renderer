package de.robolab.client.net

import de.robolab.common.net.HttpMethod
import de.robolab.common.net.headers.AuthorizationHeader

class RESTRobolabServer(
    override val hostURL: String,
    override val hostPort: Int,
    override val protocol: String
) : IRobolabServer {

    constructor(hostURL: String, hostPort: Int? = null, secure: Boolean = false) :
            this(hostURL, hostPort ?: if (secure) 443 else 80, if (secure) "https" else "http")

    private var _authHeader: AuthorizationHeader? = null
    override var authHeader: AuthorizationHeader?
        get() {
            return _authHeader
        }
        set(value) {
            _authHeader = value
            if (value == null)
                resetAuthSession()
        }

    override fun resetAuthSession() {

    }

    override suspend fun request(
        method: HttpMethod,
        path: String,
        body: String?,
        query: Map<String, String>,
        headers: Map<String, List<String>>
    ): ServerResponse {
        //TODO handleAuth, resend request if possible
        return sendHttpRequest(
            method,
            protocol,
            hostURL,
            hostPort,
            path,
            body,
            query,
            headers
        )
    }
}