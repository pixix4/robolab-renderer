package de.robolab.common.utils

private val btoa_js: (String) -> String = js("btoa").unsafeCast<(String) -> String>()
private val atob_js: (String) -> String = js("atob").unsafeCast<(String) -> String>()


actual fun String.encodeAsB64(url: Boolean): String {
    return if (url) btoa_js(this).B64ToURLB64()
    else btoa_js(this)
}

actual fun String.decodeFromB64(url: Boolean): String {
    return if (url) atob_js(this.URLB64ToB64())
    else atob_js(this)
}

actual fun String.decodeBytesFromB64(url: Boolean): ByteArray {
    if (url) return this.URLB64ToB64().decodeBytesFromB64(false)
    val chars = atob_js(this).toCharArray()
    return ByteArray(chars.size) { chars[it].code.toByte() }
}
