package de.robolab.client.utils

import kotlinx.browser.window

actual object PlatformDefaultPreferences {

    actual val mqttServerUri = "wss://mothership.inf.tu-dresden.de:9002/mqtt"

    actual val remoteServerUri = window.location.origin + window.location.pathname

    actual val fileServer = listOf("")
}
