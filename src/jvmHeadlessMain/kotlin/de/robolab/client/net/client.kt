package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*

actual val client = HttpClient(Apache)
