package de.robolab.common.utils

private val btoa: (String) -> String = js("btoa").unsafeCast<(String) -> String>()
private val atob: (String) -> String = js("atob").unsafeCast<(String) -> String>()


actual fun String.encodeAsB64(url: Boolean): String {
    return if (url) btoa(this).B64ToURLB64()
    else btoa(this)
}

actual fun String.decodeFromB64(url: Boolean): String {
    return if (url) atob(this.URLB64ToB64())
    else atob(this)
}

actual fun String.decodeBytesFromB64(url: Boolean): ByteArray {
    if (url) return this.URLB64ToB64().decodeBytesFromB64(false)
    val chars = atob(this).toCharArray()
    return ByteArray(chars.size) { chars[it].code.toByte() }
}
