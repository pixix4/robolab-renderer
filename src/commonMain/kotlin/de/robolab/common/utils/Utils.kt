package de.robolab.common.utils


fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").toLowerCase()

fun <K, V: Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    for ((key, value) in this) {
        if (value != null) result[key] = value
    }
    return result
}
