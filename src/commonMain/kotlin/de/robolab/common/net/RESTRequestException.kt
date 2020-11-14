package de.robolab.common.net

import de.robolab.client.net.IServerResponse
import de.robolab.client.net.ServerResponse
import de.robolab.client.net.mimeType
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IRESTResponse
import de.robolab.client.net.requests.RESTResult
import de.robolab.common.utils.Err
import de.robolab.common.utils.Ok
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonElement

open class RESTRequestException : RuntimeException {
    val triggeringRequest: IRESTRequest<*>?
    val triggeringResponse: IServerResponse?

    constructor(
        message: String,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message) {
        this.triggeringRequest = triggeringRequest
        this.triggeringResponse = triggeringResponse
    }

    constructor(
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super() {
        this.triggeringRequest = triggeringRequest
        this.triggeringResponse = triggeringResponse
    }

    constructor(
        message: String,
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause) {
        this.triggeringRequest = triggeringRequest
        this.triggeringResponse = triggeringResponse
    }

    constructor(
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(cause) {
        this.triggeringRequest = triggeringRequest
        this.triggeringResponse = triggeringResponse
    }
}

interface IRESTStatusProvider {
    val code: HttpStatusCode
    val message: String?
    val mimeType: MIMEType?
        get() = if (message.isNullOrEmpty()) null else MIMEType.PlainText
}

open class RESTRequestCodeException : RESTRequestException, IRESTStatusProvider {
    final override val code: HttpStatusCode
    final override val mimeType: MIMEType?

    constructor(
        code: HttpStatusCode,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse) {
        this.code = code
        this.mimeType = mimeType
    }

    constructor(
        code: HttpStatusCode,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(triggeringRequest, triggeringResponse) {
        this.code = code
        this.mimeType = null
    }

    constructor(
        code: HttpStatusCode,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse) {
        this.code = code
        this.mimeType = mimeType
    }

    constructor(
        code: HttpStatusCode, cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(cause, triggeringRequest, triggeringResponse) {
        this.code = code
        this.mimeType = null
    }

    override fun toString(): String {
        return "RESTRequestCodeError(code=$code, mimeType=$mimeType)"
    }

}

class UnexpectedMimeTypeException : RESTRequestException {

    constructor(
        actualMimeType: MIMEType,
        expectedMimeType: MIMEType,
        message: String = "Unexpected MIME-Type $actualMimeType (expected $expectedMimeType)",
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse)


    constructor(
        actualMimeType: MIMEType,
        expectedMimeType: MIMEType,
        message: String = "Unexpected MIME-Type $actualMimeType (expected $expectedMimeType)",
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse)

    constructor(
        actualMimeType: MIMEType,
        expectedMimeTypes: Collection<MIMEType>,
        message: String = "Unexpected MIME-Type $actualMimeType (expected any of $expectedMimeTypes)",
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse)


    constructor(
        actualMimeType: MIMEType,
        expectedMimeTypes: Collection<MIMEType>,
        message: String = "Unexpected MIME-Type $actualMimeType (expected any of $expectedMimeTypes)",
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse)

}

class MissingMimeTypeException : RESTRequestException {

    constructor(
        message: String = "Missing MIME-Type",
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse)


    constructor(
        message: String = "Missing MIME-Type",
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse)

    constructor(
        expectedMimeType: MIMEType,
        message: String = "Missing MIME-Type, expected $expectedMimeType",
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse)


    constructor(
        expectedMimeType: MIMEType,
        message: String = "Missing MIME-Type, expected $expectedMimeType",
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse)

    constructor(
        expectedMimeTypes: Collection<MIMEType>,
        message: String = "Missing MIME-Type, expected any of $expectedMimeTypes",
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse)


    constructor(
        expectedMimeTypes: Collection<MIMEType>,
        message: String = "Missing MIME-Type, expected any of $expectedMimeTypes",
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse)
}

class EmptyBodyRESTException : RESTRequestException {

    constructor(
        message: String,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, triggeringRequest, triggeringResponse)

    constructor(
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(triggeringRequest, triggeringResponse)

    constructor(
        message: String,
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(message, cause, triggeringRequest, triggeringResponse)

    constructor(
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(cause, triggeringRequest, triggeringResponse)

    override fun toString(): String {
        return "EmptyBodyError($triggeringRequest --> $triggeringResponse)"
    }
}

inline fun <R : IRESTResponse> parseResponseCatchingWrapper(
    response: ServerResponse,
    triggeringRequest: IRESTRequest<*>? = null,
    parser: (serverResponse: ServerResponse) -> R,
): RESTResult<R> = try {
    Ok(parser(response))
} catch (ex: Exception) {
    if (ex is RESTRequestException)
        Err(ex)
    else
        Err(RESTRequestException(ex, triggeringRequest, response))
}

inline fun <R : IRESTResponse, T : IRESTRequest<R>> parseResponseCatchingWrapper(
    response: ServerResponse,
    request: T,
    parser: (serverResponse: ServerResponse, request: T) -> R,
): RESTResult<R> = parseResponseCatchingWrapper(response, request) { it -> parser(it, request) }

fun IServerResponse.`throw`(
    triggeringRequest: IRESTRequest<*>? = null
): Nothing {
    val body = this.body
    if (body != null)
        throw RESTRequestCodeException(status, body, triggeringResponse = this, triggeringRequest = triggeringRequest)
    else
        throw RESTRequestCodeException(status, triggeringResponse = this, triggeringRequest = triggeringRequest)
}

fun IServerResponse.requireStatusCode(statusCode: HttpStatusCode, triggeringRequest: IRESTRequest<*>? = null) {
    if (status != statusCode) `throw`(triggeringRequest)
}

fun IServerResponse.requireStatusCode(
    statusCodes: Collection<HttpStatusCode>,
    triggeringRequest: IRESTRequest<*>? = null
) {
    if (statusCodes.isEmpty())
        throw IllegalArgumentException("Received empty status-code collection for response $this and request $triggeringRequest")
    if (status !in statusCodes) `throw`(triggeringRequest)
}

fun IServerResponse.requireOk(triggeringRequest: IRESTRequest<*>? = null) =
    requireStatusCode(HttpStatusCode.Ok, triggeringRequest)

fun IServerResponse.requireOkLike(triggeringRequest: IRESTRequest<*>? = null) =
    requireStatusCode(HttpStatusCode.okLikeCodes, triggeringRequest)

fun IServerResponse.requireMimeType(mimeType: MIMEType, triggeringRequest: IRESTRequest<*>? = null) {
    val actualMimeType = this.mimeType
        ?: throw MissingMimeTypeException(
            mimeType,
            triggeringResponse = this,
            triggeringRequest = triggeringRequest
        )
    if (actualMimeType != mimeType) throw UnexpectedMimeTypeException(
        actualMimeType,
        mimeType,
        triggeringResponse = this,
        triggeringRequest = triggeringRequest
    )
}

fun IServerResponse.requireMimeType(mimeTypes: Collection<MIMEType>, triggeringRequest: IRESTRequest<*>? = null) {
    if (mimeTypes.isEmpty())
        throw IllegalArgumentException("Received empty MIME-Type collection for response $this and request $triggeringRequest")
    val actualMimeType = this.mimeType
        ?: throw throw MissingMimeTypeException(
            mimeTypes,
            triggeringResponse = this,
            triggeringRequest = triggeringRequest
        )
    if (actualMimeType !in mimeTypes) throw UnexpectedMimeTypeException(
        actualMimeType,
        mimeTypes,
        triggeringResponse = this,
        triggeringRequest = triggeringRequest
    )
}

fun IServerResponse.requireBody(triggeringRequest: IRESTRequest<*>? = null, allowEmpty: Boolean = false): String {
    val body = this.body
    if (body == null || (body.isEmpty() && !allowEmpty)) throw EmptyBodyRESTException(triggeringRequest, this)
    return body
}

fun IServerResponse.requireJSONBody(triggeringRequest: IRESTRequest<*>? = null): JsonElement {
    requireBody(triggeringRequest)
    requireMimeType(MIMEType.JSON, triggeringRequest)
    return jsonBody!!
}

fun <T : Any> IServerResponse.parseOrThrow(
    deserializer: DeserializationStrategy<T>,
    triggeringRequest: IRESTRequest<*>? = null,
    ensureStatusCode: HttpStatusCode? = HttpStatusCode.Ok
): T {
    if (ensureStatusCode != null) requireStatusCode(ensureStatusCode, triggeringRequest)
    requireJSONBody(triggeringRequest)
    try {
        return parse(deserializer)!!
    } catch (ex: Exception) {
        throw if (ex is RESTRequestException)
            ex
        else
            RESTRequestException("Could not deserialize the body of $this: ${this.body}", ex, triggeringRequest, this)
    }
}


fun <T : Any> IServerResponse.parseOrThrow(
    deserializer: DeserializationStrategy<T>,
    triggeringRequest: IRESTRequest<*>? = null,
    ensureStatusCodes: Collection<HttpStatusCode>
): T {
    requireStatusCode(ensureStatusCodes, triggeringRequest)
    requireJSONBody(triggeringRequest)
    try {
        return parse(deserializer)!!
    } catch (ex: Exception) {
        throw if (ex is RESTRequestException)
            ex
        else
            RESTRequestException("Could not deserialize the body of $this", ex, triggeringRequest, this)
    }
}

fun <T : Any> IServerResponse.parseOrThrow(
    deserializer: DeserializationStrategy<T>,
    triggeringRequest: IRESTRequest<*>? = null,
    ensureStatusCode: HttpStatusCode? = HttpStatusCode.Ok,
    default: T
): T {
    if (ensureStatusCode != null) requireStatusCode(ensureStatusCode, triggeringRequest)
    if (this.body.isNullOrEmpty()) return default
    requireJSONBody(triggeringRequest)
    try {
        return parse(deserializer)!!
    } catch (ex: Exception) {
        throw if (ex is RESTRequestException)
            ex
        else
            RESTRequestException("Could not deserialize the body of $this", ex, triggeringRequest, this)
    }
}


fun <T : Any> IServerResponse.parseOrThrow(
    deserializer: DeserializationStrategy<T>,
    triggeringRequest: IRESTRequest<*>? = null,
    ensureStatusCodes: Collection<HttpStatusCode>,
    default: T
): T {
    requireStatusCode(ensureStatusCodes, triggeringRequest)
    if (this.body.isNullOrEmpty()) return default
    requireJSONBody(triggeringRequest)
    try {
        return parse(deserializer)!!
    } catch (ex: Exception) {
        throw if (ex is RESTRequestException)
            ex
        else
            RESTRequestException("Could not deserialize the body of $this", ex, triggeringRequest, this)
    }
}

fun <T, R> IServerResponse.format(vararg handlers: Pair<MIMEType, IServerResponse.(R) -> T>, triggeringRequest: R): T =
    format<T>(
        handlers.associate { it.first to { -> it.second(this, triggeringRequest) } },
        triggeringRequest as IRESTRequest<*>?
    )

fun <T, R> IServerResponse.format(handlers: Map<MIMEType, IServerResponse.(R) -> T>, triggeringRequest: R): T =
    format<T>(handlers.mapValues { { -> it.value(this, triggeringRequest) } }, triggeringRequest as IRESTRequest<*>?)

fun <T> IServerResponse.format(
    vararg handlers: Pair<MIMEType, IServerResponse.() -> T>,
    triggeringRequest: IRESTRequest<*>? = null
): T {
    val actualMimeType = this.mimeType ?: throw MissingMimeTypeException(
        handlers.map { it.first },
        triggeringRequest = triggeringRequest,
        triggeringResponse = this
    )
    val handler = (handlers.firstOrNull { it.first == mimeType } ?: throw UnexpectedMimeTypeException(
        actualMimeType,
        handlers.map { it.first },
        triggeringRequest = triggeringRequest,
        triggeringResponse = this
    )).second
    return handler(this)
}

fun <T> IServerResponse.format(
    handlers: Map<MIMEType, IServerResponse.() -> T>,
    triggeringRequest: IRESTRequest<*>? = null
): T {
    val actualMimeType = this.mimeType ?: throw MissingMimeTypeException(
        handlers.keys,
        triggeringRequest = triggeringRequest,
        triggeringResponse = this
    )
    val handler = handlers[actualMimeType] ?: throw UnexpectedMimeTypeException(
        actualMimeType,
        handlers.keys,
        triggeringRequest = triggeringRequest,
        triggeringResponse = this
    )
    return handler(this)
}