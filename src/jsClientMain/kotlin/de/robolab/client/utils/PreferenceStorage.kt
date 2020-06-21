package de.robolab.client.utils

actual object PlatformDefaultPreferences {

    actual val serverUriProperty = "wss://mothership.inf.tu-dresden.de:9002/mqtt"

    actual val fileServer = listOf("local://")
}
