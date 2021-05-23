package de.robolab.common.utils

import kotlinx.datetime.Instant

expect fun parseDateTime(dateTime: String, format: String): Instant
expect fun formatDateTime(dateTime: Instant, format: String): String

object DateFormat {
    const val FORMAT1 = "YYYY-MM-DD[T]HH:mm:ssSSS"
}
