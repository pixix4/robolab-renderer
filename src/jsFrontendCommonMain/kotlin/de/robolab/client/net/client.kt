package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import kotlinx.browser.document

actual val client: HttpClient = HttpClient(Js)

fun RequestBuilder.web(path: String? = null) {
    port(document.location?.port?.toIntOrNull() ?: 80)
    host(document.location?.hostname ?: "localhost")
    if (document.location?.protocol?.contains("https") == true) {
        secure()
    }
    if (path != null) {
        path(path)
    }
}
