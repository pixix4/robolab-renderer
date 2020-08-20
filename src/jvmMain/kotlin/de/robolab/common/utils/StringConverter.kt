package de.robolab.common.utils

import java.util.Base64

private val encoder = Base64.getEncoder()
private val urlEncoder = Base64.getUrlEncoder()
private val decoder = Base64.getDecoder()
private val urlDecoder = Base64.getUrlDecoder()

actual fun String.encodeAsB64(url: Boolean): String {
    return if (url) urlEncoder.encodeToString(this.toByteArray())
    else encoder.encodeToString(this.toByteArray())
}

actual fun String.decodeFromB64(url: Boolean): String {
    return String(
        if (url) urlDecoder.decode(this.toByteArray())
        else decoder.decode(this.toByteArray())
    )
}