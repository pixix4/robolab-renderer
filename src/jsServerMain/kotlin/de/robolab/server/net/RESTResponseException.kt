package de.robolab.server.net

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType

open class RESTResponseException : RuntimeException {

    val mimeType: MIMEType?

    constructor(
        message: String,
        mimeType: MIMEType = MIMEType.PlainText
    ) : super(message) {
        this.mimeType = mimeType
    }

    constructor() : super() {
        this.mimeType = null
    }

    constructor(
        message: String,
        mimeType: MIMEType = MIMEType.PlainText,
        cause: Throwable?
    ) : super(message, cause) {
        this.mimeType = mimeType
    }

    constructor(
        cause: Throwable?
    ) : super(cause) {
        this.mimeType = null
    }
}