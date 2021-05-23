package de.robolab.common.net.headers

import de.robolab.common.utils.formatDateTime
import de.robolab.common.utils.parseDateTime
import kotlinx.datetime.Instant

class LastModifiedHeader(value: String) : Header(name, value) {
    constructor(value: Instant) : this(formatDateTime(value, FORMAT).replace("UTC", "GMT"))

    val dateTime: Instant = parseDateTime(value, FORMAT)

    companion object {
        const val FORMAT = "ddd, DD MMM YYYY HH:mm:ss z"
        const val name: String = "last-modified"
    }
}
