package de.robolab.common.utils

import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.common.planet.test.PlanetTestGoal
import kotlin.jvm.JvmName
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").lowercase()

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
    transform: (a: T, b: R, c: S) -> V,
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

inline fun <T, K, V> Iterable<T>.associateNotNull(transform: (T) -> Pair<K, V>?) = mapNotNull(transform).toMap()

inline fun <reified V, T> Iterable<T>.partitionIsInstance(): Pair<List<V>, List<T>> where V : T {
    val (hits, misses) = this.partition { it is V }
    return hits.filterIsInstance<V>() to misses
}

inline fun <reified V1, reified V2, T> Iterable<T>.partitionIsInstance2(): Triple<List<V1>, List<V2>, List<T>> where V1 : T, V2 : T {
    val (hits1, misses1) = partitionIsInstance<V1, T>()
    val (hits2, misses2) = misses1.partitionIsInstance<V2, T>()
    return Triple(hits1, hits2, misses2)
}

private val durationRegex: Regex = "^(\\d+)([dhms]|ms)$".toRegex(RegexOption.IGNORE_CASE)

@ExperimentalTime
fun String.toDuration(): Duration {
    val (count, unit) = (durationRegex.matchEntire(this)
        ?: throw IllegalArgumentException("Could not parse duration-string \"$this\"")).destructured
    return count.toLong().toDuration(
        when (unit.lowercase()) {
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

fun Number.toFixed(places: Int): String {
    if (places == 0) {
        return toLong().toString()
    }

    val exp = 10.0.pow(places)
    val number = (round(toDouble() * exp) / exp).toString()

    val dotIndex = number.indexOf('.')

    if (dotIndex < 0) {
        return number + '.' + "0".repeat(places)
    }

    val missingPlaces = dotIndex + places - number.lastIndex

    if (missingPlaces == 0) {
        return number
    }

    if (missingPlaces > 0) {
        return number + "0".repeat(missingPlaces)
    }

    return number.dropLast(missingPlaces.absoluteValue)
}

inline fun <T, R> List<T>.getInterpolated(index: Double, interpolator: (T, T, Double) -> R): R {
    if (isEmpty()) throw IllegalArgumentException("Interpolation-Index out of range (empty list): $index")
    if (index < 0 || index > lastIndex) throw IllegalArgumentException("Interpolation-Index out of range (0..$lastIndex): $index ")
    val lower = this[floor(index).toInt()]
    val upper = this[ceil(index).toInt()]
    val subProgress = index % 1
    return interpolator(lower, upper, subProgress)
}

fun <T> List<T>.getInterpolated(index: Double): T where T : IInterpolatable<T> = getInterpolated(index) { p1, p2, f ->
    p1.interpolate(p2, f)
}

inline fun <T> MutableList<T>.consumeEach(action: (T) -> Unit, fromBack: Boolean = false) {
    while (this.isNotEmpty()) {
        val element = if (fromBack) last() else first()
        action(element) //Only remove after action has been completed in case of exception
        if (fromBack) removeLast() else removeFirst()
    }
}
