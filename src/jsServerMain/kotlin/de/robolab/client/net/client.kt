package de.robolab.client.net

import io.ktor.client.HttpClient

actual val client : HttpClient
    get() = throw UnsupportedOperationException("Server cannot send messages")