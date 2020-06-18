package de.robolab.common.net.headers

import com.soywiz.klock.*

class LastModifiedHeader(value: String) : Header(name, value) {
    constructor(value: DateTime) : this(format.format(value.toOffset(offset)).replace("UTC", "GMT"))

    val dateTime: DateTime = format.parseUtc(value)

    companion object {
        val format: DateFormat = DateFormat.DEFAULT_FORMAT
        val offset: TimezoneOffset = TimezoneOffset(TimezoneNames.DEFAULT.namesToOffsets["GMT"]!!)
        const val name: String = "last-modified"
    }
}