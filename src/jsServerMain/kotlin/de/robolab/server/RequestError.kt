package de.robolab.server

import de.robolab.client.net.ServerResponse
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType

class RequestError : RuntimeException {
    val code: HttpStatusCode
    val mimeType: MIMEType?
    val verbose: Boolean

    constructor(
        code: HttpStatusCode = HttpStatusCode.InternalServerError,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        verbose: Boolean = true
    ) : super(message) {
        this.code = code
        this.mimeType = mimeType
        this.verbose = verbose
    }

    constructor(code: HttpStatusCode = HttpStatusCode.InternalServerError, verbose: Boolean = true) : super() {
        this.code = code
        this.mimeType = null
        this.verbose = verbose
    }
}

fun ServerResponse.`throw`(): Nothing {
    if (this.body != null)
        throw RequestError(status, this.body)
    else
        throw RequestError(status)
}