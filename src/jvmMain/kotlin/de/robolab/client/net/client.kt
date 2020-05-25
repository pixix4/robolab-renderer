package de.robolab.client.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache

actual val client = HttpClient(Apache)