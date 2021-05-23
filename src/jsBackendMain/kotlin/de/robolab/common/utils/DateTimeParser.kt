package de.robolab.common.utils

import kotlinx.datetime.Instant
import kotlin.js.Date

external interface MomentJs {
    fun format(format: String): String
    fun valueOf(): Number
}

@JsModule("moment")
@JsNonModule
external fun moment(dateTime: String, format: String): MomentJs

@JsModule("moment")
@JsNonModule
external fun moment(timestamp: dynamic): MomentJs

actual fun parseDateTime(dateTime: String, format: String): Instant {
    return Instant.fromEpochMilliseconds(moment(dateTime, format).valueOf().toLong())
}

actual fun formatDateTime(dateTime: Instant, format: String): String {
    return moment(Date(dateTime.toEpochMilliseconds())).format(format)
}
