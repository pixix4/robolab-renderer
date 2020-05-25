package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual val client: HttpClient = HttpClient(Js)