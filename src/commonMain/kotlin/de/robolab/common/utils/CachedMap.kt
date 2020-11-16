package de.robolab.common.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

open class CachedMap<K, V> protected constructor(protected val map: MutableMap<K, CachedValue<V>>) {

    constructor() : this(mutableMapOf())

    protected val accessMutex: Mutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    //use cast instead of !! because T might be nullable
    suspend fun getStoredMap(): Map<K, V> {
        return accessMutex.withLock { map.toMap() }
            .mapValues { it.value.getStoredValue() }
            .filterValues { it.first }
            .mapValues { it.value.second as V }
    }

    @Suppress("UNCHECKED_CAST")
    //use cast instead of !! because T might be nullable
    suspend fun getStoredMap(maxAge: Duration): Map<K, V> {
        return accessMutex.withLock { map.toMap() }
            .mapValues { it.value.getStoredValue(maxAge) }
            .filterValues { it.first }
            .mapValues { it.value.second as V }
    }

    suspend fun getUpdatedMap(): Map<K, V> {
        return accessMutex.withLock { map.toMap() }.mapValues { it.value.getValue() }
    }

    suspend fun getUpdatedMap(maxAge: Duration): Map<K, V> {
        return accessMutex.withLock { map.toMap() }.mapValues { it.value.getValue(maxAge) }
    }

    open suspend fun getStored(key: K): V? {
        return accessMutex.withLock { map[key] }?.getStoredValue()?.second
    }

    open suspend fun getStored(key: K, maxAge: Duration): V? {
        return accessMutex.withLock { map[key] }?.getStoredValue(maxAge)?.second
    }

    open suspend fun tryGetStored(key: K): Pair<Boolean, V?>? {
        return accessMutex.withLock { map[key] }?.getStoredValue()
    }

    open suspend fun tryGetStored(key: K, maxAge: Duration): Pair<Boolean, V?>? {
        return accessMutex.withLock { map[key] }?.getStoredValue(maxAge)
    }

    open suspend fun getUpdated(key: K): V? {
        return accessMutex.withLock { map[key] }?.getValue()
    }

    open suspend fun getUpdated(key: K, maxAge: Duration): V? {
        return accessMutex.withLock { map[key] }?.getValue(maxAge)
    }

    open suspend fun getOrPutCache(key: K, factory: () -> CachedValue<V>): CachedValue<V> {
        return accessMutex.withLock { map.getOrPut(key, factory) }
    }
}

class DefaultCachedMap<K, V>(val factory: (K) -> CachedValue<V>) : CachedMap<K, V>() {

    override suspend fun getUpdated(key: K): V {
        return accessMutex.withLock { map.getOrPut(key) { factory(key) } }.getValue()
    }

    override suspend fun getUpdated(key: K, maxAge: Duration): V {
        return accessMutex.withLock { map.getOrPut(key) { factory(key) } }.getValue(maxAge)
    }
}

suspend fun <K, V> CachedMap<K, V>.getOrPut(key: K, producer: suspend () -> V): V =
    getOrPutCache(key) { cachedValue(producer) }.getValue()

suspend fun <K, V> CachedMap<K, V>.getOrPut(key: K, default: V, producer: suspend () -> V): V =
    getOrPutCache(key) { cachedValue(default, producer) }.getValue()

suspend fun <K, V> CachedMap<K, V>.getOrPut(key: K, cacheDuration: Duration, producer: suspend () -> V): V =
    getOrPutCache(key) { cachedValue(cacheDuration, producer) }.getValue()

suspend fun <K, V> CachedMap<K, V>.getOrPut(
    key: K,
    cacheDuration: Duration,
    default: V,
    defaultDuration: Duration = cacheDuration,
    producer: suspend () -> V,
): V = getOrPutCache(key) { cachedValue(cacheDuration, default, defaultDuration, producer) }.getValue()

suspend fun <K, V> CachedMap<K, V>.getOrPutAged(key: K, maxAge: Duration, producer: suspend () -> V): V =
    getOrPutCache(key) { cachedValue(producer) }.getValue(maxAge)

suspend fun <K, V> CachedMap<K, V>.getOrPutAged(key: K, maxAge: Duration, default: V, producer: suspend () -> V): V =
    getOrPutCache(key) { cachedValue(default, producer) }.getValue(maxAge)

suspend fun <K, V> CachedMap<K, V>.getOrPutAged(
    key: K,
    maxAge: Duration,
    cacheDuration: Duration,
    producer: suspend () -> V
): V = getOrPutCache(key) { cachedValue(cacheDuration, producer) }.getValue(maxAge)

suspend fun <K, V> CachedMap<K, V>.getOrPutAged(
    key: K,
    maxAge: Duration,
    cacheDuration: Duration,
    default: V,
    defaultDuration: Duration = cacheDuration,
    producer: suspend () -> V,
): V = getOrPutCache(key) { cachedValue(cacheDuration, default, defaultDuration, producer) }.getValue(maxAge)

fun <K, V> cachedMap() = CachedMap<K, V>()
fun <K, V> cachedMap(factory: (K) -> CachedValue<V>) = DefaultCachedMap(factory)
inline fun <K, V> cachedMap(crossinline producer: suspend (K) -> V) =
    DefaultCachedMap<K, V> { cachedValue { producer(it) } }

inline fun <K, V> cachedMap(default: V, crossinline producer: suspend (K) -> V) =
    DefaultCachedMap<K, V> { cachedValue(default) { producer(it) } }

inline fun <K, V> cachedMap(crossinline defaultProducer: (K) -> V, crossinline producer: suspend (K) -> V) =
    DefaultCachedMap<K, V> { cachedValue(defaultProducer(it)) { producer(it) } }

inline fun <K, V> cachedMap(duration: Duration, crossinline producer: suspend (K) -> V) =
    DefaultCachedMap<K, V> { cachedValue(duration) { producer(it) } }

inline fun <K, V> cachedMap(
    duration: Duration,
    default: V,
    defaultDuration: Duration = duration,
    crossinline producer: suspend (K) -> V
) = DefaultCachedMap<K, V> { cachedValue(duration, default, defaultDuration) { producer(it) } }

inline fun <K, V> cachedMap(
    duration: Duration,
    crossinline defaultProducer: (K) -> V,
    defaultDuration: Duration = duration,
    crossinline producer: suspend (K) -> V,
) = DefaultCachedMap<K, V> { cachedValue(duration, defaultProducer(it), defaultDuration) { producer(it) } }
