package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual val client: HttpClient = HttpClient(Js)

actual suspend fun pingRemote(
    scheme: String,
    host: String,
    port: Int,
    path: String
): Boolean {
    val url = buildString {
        append(scheme)
        append("://")
        append(host)

        if (!(scheme == "http" && port == 80 || scheme == "https" && port == 443 || port == 0)) {
            append(":")
            append(port)
        }

        if (!path.startsWith("/")) {
            append("/")
        }
        append(path)
    }

    val options = js("{}")
    options.method = "HEAD"

    val promise = window.fetch(url, options)

    return suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
        promise.then {
            cont.resume(it.status in 200..399)
        }.catch {
            cont.resume(false)
        }
    }
}

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
