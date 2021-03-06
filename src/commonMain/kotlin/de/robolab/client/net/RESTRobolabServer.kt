package de.robolab.client.net

import de.robolab.client.app.model.file.requestAuthToken
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.utils.Logger
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
    val enableWaitingRequestList: Boolean = false,
) : IRobolabServer {

    constructor(hostURL: String, hostPort: Int? = null, secure: Boolean = false) :
            this(hostURL, hostPort ?: if (secure) 443 else 80, if (secure) "https" else "http")

    private val headerHashcodeXorMask: Int = Random.nextInt()
    private val logger: Logger = Logger("ServerLogger")

    override val authHeaderProperty = property<AuthorizationHeader>()
    override var authHeader by authHeaderProperty

    override fun resetAuthSession() {

    }

    private var _requestAuthTokenMutex: Mutex = Mutex()
    private var _waitingRequestsMutex: Mutex = Mutex()
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

        GlobalScope.launch {
            val header = loadAuthorizationHeader()
            if (header != null) {
                authHeader = header
            }
        }
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
            } while (requestAuthToken(this, true))
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
