package de.robolab.client.utils

actual object PlatformDefaultPreferences {

    actual val serverUriProperty = "ssl://mothership.inf.tu-dresden.de:8883"

    actual val fileServer = listOf(
        "directory://planet"
    )
}
