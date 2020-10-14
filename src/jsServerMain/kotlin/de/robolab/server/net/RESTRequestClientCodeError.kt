package de.robolab.server.net

import de.robolab.client.net.IServerResponse
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.RESTRequestCodeError

class RESTRequestClientCodeError : RESTRequestCodeError {
    constructor(
        code: HttpStatusCode,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(code, message, mimeType, triggeringRequest, triggeringResponse)

    constructor(
        code: HttpStatusCode,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(code, triggeringRequest, triggeringResponse)

    constructor(
        code: HttpStatusCode,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(code, message, mimeType, cause, triggeringRequest, triggeringResponse)

    constructor(
        code: HttpStatusCode, cause: Throwable?,
        triggeringRequest: IRESTRequest<*>? = null,
        triggeringResponse: IServerResponse? = null
    ) : super(code, cause, triggeringRequest, triggeringResponse)
}