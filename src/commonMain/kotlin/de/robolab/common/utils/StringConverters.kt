@file:Suppress("FunctionName")

package de.robolab.common.utils

expect fun String.encodeAsB64(url: Boolean = false): String

expect fun String.decodeFromB64(url: Boolean = false): String

internal fun String.B64ToURLB64(): String = this
    .replace('+', '-')
    .replace('/', '_')
    .replace("""=+$""".toRegex(), "") //removes all trailing '=', which should be all
    .replace("=", "%3d")

internal fun String.URLB64ToB64(): String = this
    .replace('-', '+')
    .replace('_', '/')
    .replace("%3d", "=")
