package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*

actual val client = HttpClient(OkHttp)
