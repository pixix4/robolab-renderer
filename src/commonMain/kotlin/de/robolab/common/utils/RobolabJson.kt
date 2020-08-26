package de.robolab.common.utils

import kotlinx.serialization.json.Json

val RobolabJson = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
}
