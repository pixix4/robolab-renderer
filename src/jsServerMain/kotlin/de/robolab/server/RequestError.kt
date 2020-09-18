package de.robolab.server

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType

class RequestError : RuntimeException {
    val code: HttpStatusCode
    val mimeType: MIMEType?

    constructor(
        code: HttpStatusCode = HttpStatusCode.InternalServerError,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText
    ) : super(message) {
        this.code = code
        this.mimeType = mimeType
    }

    constructor(code: HttpStatusCode = HttpStatusCode.InternalServerError) : super() {
        this.code = code
        this.mimeType = null
    }
}