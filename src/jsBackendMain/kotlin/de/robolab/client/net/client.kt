package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.*

actual val client: HttpClient = HttpClient(Js)
actual suspend fun pingRemote(
    scheme: String,
    host: String,
    port: Int,
    path: String
): Boolean {
    TODO("Not yet implemented")
}