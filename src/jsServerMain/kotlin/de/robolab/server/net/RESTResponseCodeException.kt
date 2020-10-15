package de.robolab.server.net

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType

open class RESTResponseCodeException : RESTResponseException {
    val code: HttpStatusCode

    constructor(
        code: HttpStatusCode,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText
    ) : super(message, mimeType) {
        this.code = code
    }

    constructor(
        code: HttpStatusCode
    ) : super() {
        this.code = code
    }

    constructor(
        code: HttpStatusCode,
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        cause: Throwable?
    ) : super(message, mimeType, cause) {
        this.code = code
    }

    constructor(
        code: HttpStatusCode, cause: Throwable?
    ) : super(cause){
        this.code = code
    }
}