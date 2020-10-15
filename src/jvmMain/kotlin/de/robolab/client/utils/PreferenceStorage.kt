package de.robolab.client.utils

actual object PlatformDefaultPreferences {

    actual val mqttServerUri = "ssl://mothership.inf.tu-dresden.de:8883"

    actual val remoteServerUri = "https://robolab.inf.tu-dresden.de/renderer/"

    actual val fileServer = listOf<String>()
}
