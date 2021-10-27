package de.robolab.client.net

import de.robolab.client.app.model.file.handleAuthPrompt
import de.robolab.client.net.requests.auth.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class RESTRobolabServer(
    override val hostURL: String,
    override val hostPort: Int,
    override val protocol: String,
    val oidcServer: OIDCServer,
    val clientID: String,
    private val clientSecret: String? = null,
    val enableWaitingRequestList: Boolean = false,
) : IRobolabServer {

    constructor(
        hostURL: String,
        hostPort: Int? = null,
        secure: Boolean = false,
        oidcServer: OIDCServer,
        clientID: String,
        clientSecret: String? = null
    ) : this(
        hostURL,
        hostPort ?: if (secure) 443 else 80,
        if (secure) "https" else "http",
        oidcServer,
        clientID,
        clientSecret
    )

    private val headerHashcodeXorMask: Int = Random.nextInt()
    private val logger: Logger = Logger("ServerLogger")

    private val refreshTokenProperty = property<String>()
    private var refreshToken by refreshTokenProperty

    override val authHeaderProperty = property<AuthorizationHeader>()
    override var authHeader by authHeaderProperty

    override fun resetAuthSession() {
        val owner = Random.nextLong()
        GlobalScope.launch {
            _requestAuthTokenMutex.withLock(owner) {
                requestToken()
                //TODO: improve queue-handling, only request new Token if previously recorded token has changed (see `usedHeader` in `request()`)
            }
        }
    }

    private val _requestAuthTokenMutex: Mutex = Mutex(true) //locked for param-loading from storage
    private val _waitingRequestsMutex: Mutex = Mutex()
    private val _waitingRequests: MutableList<Triple<Any, HttpMethod, String>> = mutableListOf()
    val waitingRequests: List<Pair<HttpMethod, String>>
        get() = _waitingRequests.map { it.second to it.third }

    init {
        authHeaderProperty.onChange {
            if (authHeader == null) {
                resetAuthSession()
            }

            GlobalScope.launch {
                storeAuthorizationHeader(authHeader)
            }
        }
        refreshTokenProperty.onChange {
            GlobalScope.launch {
                storeRefreshToken(refreshToken)
            }
        }


        GlobalScope.launch {
            try {
                val header = loadAuthorizationHeader()
                if (header != null) {
                    authHeader = header
                }
                val refresh = loadRefreshToken()
                if (refresh != null)
                    refreshToken = refresh
            } finally {
                _requestAuthTokenMutex.unlock() // Locked by initializer
            }
        }
    }

    private suspend fun requestToken(): Boolean {
        val refreshToken = refreshToken
        if (!refreshToken.isNullOrEmpty()) {
            val refreshResponse: Any = try { //refreshResponse: Union<TokenResponse,Exception>
                oidcServer.performTokenRefresh(refreshToken, clientID, clientSecret)
            } catch (ex: IllegalStateException) {
                if (ex.message?.startsWith("Fail to serialize body.") != true) throw ex
                ex
            }
            when (refreshResponse) {
                is TokenResponse.FinalTokenResponse.AccessToken -> {
                    useAccessToken(refreshResponse, refreshToken)
                    return true
                }
                !is TokenResponse.ExpiredToken -> {
                    logger.warn("Unexpected refresh-response:", refreshResponse)
                }
            }
        }


        val authResponse: TokenResponse.FinalTokenResponse.AccessToken = try {
            oidcServer.performDeviceAuth(clientID, clientSecret, promptHandler = ::handleAuthPrompt)
        } catch (ex: IllegalStateException) {
            if (ex.message?.startsWith("Fail to serialize body.") == true) {
                logger.error { "Unexpected response to device-auth" }
                logger.error { "|C-x-A| AUTH Exception!" }
                throw IllegalStateException("Unexpected response to device-auth", ex)
            }
            throw ex
        }
        useAccessToken(authResponse)
        return true
    }

    private fun useAccessToken(token: TokenResponse.FinalTokenResponse.AccessToken, refreshToken: String? = null) {
        authHeaderProperty.set(AuthorizationHeader.Bearer(token.accessToken))
        refreshTokenProperty.set(token.refreshToken ?: refreshToken)
    }

    override suspend fun request(
        method: HttpMethod,
        path: String,
        body: String?,
        query: Map<String, String>,
        headers: Map<String, List<String>>
    ): ServerResponse {
        if (headers.keys.any { it.lowercase() == AuthorizationHeader.name }) {
            //Auth-Header overwritten, do not attempt multiple requests
            logger.debug {
                "|C==>S| $method:$path HED: {custom auth}"
            }
            return sendHttpRequest(
                method,
                protocol,
                hostURL,
                hostPort,
                path,
                body,
                query,
                headers,
                throwOnNonOk = false
            )
        }
        val owner: Any = Random.nextLong()
        var usedHeader: AuthorizationHeader?
        var response: ServerResponse
        var waitingRequestMaxIndex: Int = -1
        var waitingRequest: Triple<Any, HttpMethod, String>? = null
        suspend fun removeWaitingRequest() {
            if (waitingRequestMaxIndex >= 0) {
                _waitingRequestsMutex.withLock {
                    var currentIndex = _waitingRequests.subList(0, waitingRequestMaxIndex).indexOf(waitingRequest)
                    if (currentIndex < 0) {
                        currentIndex = _waitingRequests.indexOf(waitingRequest)
                    }
                    if (currentIndex >= 0) {
                        val removedRequest = _waitingRequests.removeAt(currentIndex)
                        if (removedRequest != waitingRequest) {
                            _waitingRequests.add(currentIndex, removedRequest)
                            throw IllegalStateException("Tried to remove request $waitingRequest at index $currentIndex but removed $removedRequest instead")
                        }
                    }
                }
            }
        }
        try {
            var logAuthTokenProvided = false
            do {
                if (logAuthTokenProvided)
                    logger.debug {
                        "|C<@-A| $method:$path AUTH Provided"
                    }
                if (enableWaitingRequestList)
                    removeWaitingRequest()
                do {
                    usedHeader = authHeader
                    if (_requestAuthTokenMutex.holdsLock(owner))
                        _requestAuthTokenMutex.unlock(owner)
                    logger.debug {
                        "|C-->S| $method:$path HED: ${
                            usedHeader?.let {
                                "${it.schemaName}#${it.hashCode() xor headerHashcodeXorMask}"
                            }
                        }"
                    }
                    response = sendHttpRequest(
                        method,
                        protocol,
                        hostURL,
                        hostPort,
                        path,
                        body,
                        query,
                        if (usedHeader == null) headers
                        else headers + (AuthorizationHeader.name to listOf(usedHeader.value)),
                        throwOnNonOk = false
                    )
                    logger.debug {
                        "|C<--S| $method:$path - ${response.metaInfoString()}; CNT:${response.bodyInfoString()}"
                    }
                    if (response.status != HttpStatusCode.Unauthorized)
                        return response
                    if (enableWaitingRequestList)
                        _waitingRequestsMutex.withLock {
                            waitingRequestMaxIndex = _waitingRequests.size
                            if (waitingRequest == null)
                                waitingRequest = Triple(owner, method, path)
                            _waitingRequests.add(waitingRequest!!)
                        }
                    _requestAuthTokenMutex.lock(owner)
                } while (usedHeader != authHeader)

                logger.debug {
                    "|C-@>A| $method:$path AUTH"
                }
                logAuthTokenProvided = true
            } while (requestToken())
        } finally {
            if (_requestAuthTokenMutex.holdsLock(owner))
                _requestAuthTokenMutex.unlock(owner)
            if (enableWaitingRequestList)
                removeWaitingRequest()
        }
        logger.debug {
            "|C-!-A| $method:$path AUTH Cancelled"
        }
        return response //user canceled the requestAuthToken-Call
    }
}

expect suspend fun loadAuthorizationHeader(): AuthorizationHeader?
expect suspend fun storeAuthorizationHeader(header: AuthorizationHeader?)
expect suspend fun loadRefreshToken(): String?
expect suspend fun storeRefreshToken(refreshToken: String?)
