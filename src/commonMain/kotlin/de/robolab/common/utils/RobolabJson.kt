package de.robolab.common.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

val RobolabJson = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
}

fun <T> DeserializationStrategy<T>.decode(element:JsonElement) = RobolabJson.decodeFromJsonElement(this, element)
fun <T> SerializationStrategy<T>.encode(value: T) = RobolabJson.encodeToJsonElement(this, value)