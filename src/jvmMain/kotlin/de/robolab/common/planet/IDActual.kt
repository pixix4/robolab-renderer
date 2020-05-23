package de.robolab.common.planet

actual fun ID.decode(): String = throw UnsupportedOperationException("Clients cannot decode ids")

actual fun String.toID():ID = throw UnsupportedOperationException("Clients cannot encode ids")