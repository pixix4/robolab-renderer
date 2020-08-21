package de.robolab.common.utils

import kotlin.jvm.JvmName


fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").toLowerCase()

fun <K, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    for ((key, value) in this) {
        if (value != null) result[key] = value
    }
    return result
}

fun <K, V> Map<K, V>.withEntry(entry: Pair<K, V>): Map<K, V> =
    withEntry(entry) { key, oldValue, newValue -> throw IllegalArgumentException("No merger specified, map already contains an entry with key $key and value $oldValue when adding $newValue") }


fun <K, V> Map<K, V>.withEntry(entry: Pair<K, V>, merger:(key: K, oldValue: V, newValue: V)-> V): Map<K, V> {
    val oldValue: V = this[entry.first] ?: return (this + entry)
    return this + (entry.first to merger(entry.first, oldValue,entry.second))
}

@JvmName("withEntryList")
fun <K, V> Map<K, List<V>>.withEntry(entry: Pair<K, V>) = this.withEntry(entry.first to listOf(entry.second)){ _:K, oldValue:List<V>, newValue:List<V> ->
    oldValue + newValue
}
