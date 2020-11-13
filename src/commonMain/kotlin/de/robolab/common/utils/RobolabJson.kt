package de.robolab.common.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

val RobolabJson = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
}

fun <T> DeserializationStrategy<T>.decode(element: JsonElement) = RobolabJson.decodeFromJsonElement(this, element)
fun <T> DeserializationStrategy<T>.decodeString(string: String) = RobolabJson.decodeFromString(this, string)
fun <T> SerializationStrategy<T>.encode(value: T) = RobolabJson.encodeToJsonElement(this, value)
fun <T> SerializationStrategy<T>.encodeString(value: T) = RobolabJson.encodeToString(this, value)

inline fun <reified T> decode(element: JsonElement) = RobolabJson.decodeFromJsonElement<T>(element)
inline fun <reified T> decodeString(string: String) = RobolabJson.decodeFromString<T>(string)
inline fun <reified T> encode(value: T) = RobolabJson.encodeToJsonElement(value)
inline fun <reified T> encodeString(value: T) = RobolabJson.encodeToString(value)
