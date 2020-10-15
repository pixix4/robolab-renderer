package de.robolab.common.net

import de.robolab.client.net.IServerResponse
import de.robolab.client.net.ServerResponse
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IRESTResponse
import de.robolab.client.net.requests.RESTResult
import de.robolab.common.utils.Err
import de.robolab.common.utils.Ok

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

open class RESTRequestCodeException : RESTRequestException {
    val code: HttpStatusCode
    val mimeType: MIMEType?

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

inline fun <R : IRESTResponse> parseResponseCatchingWrapper(
    response: ServerResponse,
    triggeringRequest: IRESTRequest<*>? = null,
    parser: (serverResponse: ServerResponse) -> R,
): RESTResult<R> = try {
    Ok(parser(response))
} catch (ex: Exception) {
    if(ex is RESTRequestException)
        Err(ex)
    else
        Err(RESTRequestException(ex,triggeringRequest,response))
}

inline fun <R : IRESTResponse, T: IRESTRequest<R>> parseResponseCatchingWrapper(
    response: ServerResponse,
    request: T,
    parser: (serverResponse: ServerResponse, request: T) -> R,
): RESTResult<R> = parseResponseCatchingWrapper(response,request){it -> parser(it,request)}

fun IServerResponse.`throw`(
    triggeringRequest: IRESTRequest<*>?=null
): Nothing {
    val body = this.body
    if (body != null)
        throw RESTRequestCodeException(status, body,triggeringResponse = this, triggeringRequest = triggeringRequest)
    else
        throw RESTRequestCodeException(status, triggeringResponse = this, triggeringRequest = triggeringRequest)
}