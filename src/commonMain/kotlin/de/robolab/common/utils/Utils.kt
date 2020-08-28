package de.robolab.common.utils

import de.robolab.common.planet.Path
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

inline fun <T, R, S, V> Iterable<T>.zip(other1: Iterable<R>, other2: Iterable<S>, transform: (a: T, b: R, c: S) -> V): List<V> {
    val first = iterator()
    val second = other1.iterator()
    val third = other2.iterator()

    val firstLength = if (this is Collection<*>) this.size else 10
    val secondLength = if (other1 is Collection<*>) other1.size else 10
    val thirdLength = if (other2 is Collection<*>) other2.size else 10

    val list = ArrayList<V>(minOf(firstLength, secondLength, thirdLength))
    while (first.hasNext() && second.hasNext() && third.hasNext()) {
        list.add(transform(first.next(), second.next(), third.next()))
    }
    return list
}

fun Iterable<Path>.pathIntersect(other:Iterable<Path>):Iterable<Path>{
    val otherCopy = other.toList()
    return filter{selfPath->
        otherCopy.any(selfPath::equalPath)
    }
}