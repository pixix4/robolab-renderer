package de.robolab.server.jsutils

import de.robolab.server.externaljs.http.ServerResponse
import de.robolab.common.net.HttpStatusCode

var ServerResponse.httpStatusCode: HttpStatusCode?
    get() = HttpStatusCode.get(this.statusCode)
    set(value) {
        this.statusCode = (value ?: HttpStatusCode.InternalServerError).code
    }

