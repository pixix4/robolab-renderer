package de.robolab.common.utils

import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").toLowerCase()

fun <K, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    for ((key, value) in this) {
        if (value != null) result[key] = value
    }
    return result
}

fun <K : Any, V> Map<K?, V>.filterKeysNotNull(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    for ((key, value) in this) {
        if (key != null) result[key] = value
    }
    return result
}

fun <K, V> Map<K, V>.withEntry(entry: Pair<K, V>): Map<K, V> =
    withEntry(entry) { key, oldValue, newValue -> throw IllegalArgumentException("No merger specified, map already contains an entry with key $key and value $oldValue when adding $newValue") }


fun <K, V> Map<K, V>.withEntry(entry: Pair<K, V>, merger: (key: K, oldValue: V, newValue: V) -> V): Map<K, V> {
    val oldValue: V = this[entry.first] ?: return (this + entry)
    return this + (entry.first to merger(entry.first, oldValue, entry.second))
}

@JvmName("withEntryList")
fun <K, V> Map<K, List<V>>.withEntry(entry: Pair<K, V>) =
    this.withEntry(entry.first to listOf(entry.second)) { _: K, oldValue: List<V>, newValue: List<V> ->
        oldValue + newValue
    }

inline fun <T, R, S, V> Iterable<T>.zip(
    other1: Iterable<R>,
    other2: Iterable<S>,
    transform: (a: T, b: R, c: S) -> V
): List<V> {
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

inline fun <T> Iterable<T>.intersect(other: Iterable<T>, predicate: (T, T) -> Boolean): Iterable<T> {
    return filter { selfPath ->
        other.any { predicate(selfPath, it) }
    }
}

private val durationRegex: Regex = "^(\\d+)([dhms]|ms)$".toRegex(RegexOption.IGNORE_CASE)

@ExperimentalTime
fun String.toDuration(): Duration {
    val (count, unit) = (durationRegex.matchEntire(this)
        ?: throw IllegalArgumentException("Could not parse duration-string \"$this\"")).destructured
    return count.toLong().toDuration(
        when (unit.toLowerCase()) {
            "d" -> DurationUnit.DAYS
            "h" -> DurationUnit.HOURS
            "m" -> DurationUnit.MINUTES
            "s" -> DurationUnit.SECONDS
            "ms" -> DurationUnit.MILLISECONDS
            else -> throw IllegalArgumentException("Cannot parse unit \"$unit\" from duration-string \"$this\"")
        }
    )
}

inline fun <T> copyToCount(seed: T, count: Int, copy: (T) -> T): List<T> =
    when {
        count < 0 -> throw IllegalArgumentException("Cannot clone to a negative count ($count)")
        count == 0 -> emptyList()
        count == 1 -> listOf(seed)
        else -> {
            listOf(seed) + ((2..count).map { copy(seed) })
        }
    }

inline fun <T, R, V> List<T>.zipWithCopies(seed: R, copy: (R) -> R, transform: (a: T, b: R) -> V): List<V> =
    zip(copyToCount(seed, size, copy), transform)
