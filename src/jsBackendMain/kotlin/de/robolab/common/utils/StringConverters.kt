
package de.robolab.common.utils

import de.robolab.common.externaljs.Buffer

private fun btoa(str: String, encoding: String = "utf8"): String {
    return Buffer.from(
        str, encoding
    ).toString("base64")
}

private fun atob(str: String, encoding: String = "utf8"): String {
    return Buffer.from(
        str, "base64"
    ).toString(encoding)
}


actual fun String.encodeAsB64(url: Boolean): String {
    return if (url) btoa(this).B64ToURLB64()
    else btoa(this)
}

actual fun String.decodeFromB64(url: Boolean): String {
    return if (url) atob(this.URLB64ToB64())
    else atob(this)
}