package de.robolab.common.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class CachedValue<T>(val duration: Duration, private val producer: suspend () -> T) {

    constructor(duration: Duration, producer: suspend () -> T, defaultValue: T, defaultDuration: Duration) : this(
        duration,
        producer
    ) {
        storedValue = defaultValue
        timeout = TimeSource.Monotonic.markNow().plus(defaultDuration)
    }

    private var storedValue: T? = null
    private var timeout: TimeMark? = null
    private val accessMutex: Mutex = Mutex()

    val durationRemaining: Duration
        get() = -(timeout?.elapsedNow() ?: Duration.INFINITE)

    val age: Duration
        get() = (timeout?.elapsedNow() ?: Duration.INFINITE) + duration


    @Suppress("UNCHECKED_CAST")
    //use cast instead of !! because T might be nullable
    suspend fun getValue(): T {
        if (timeout?.hasNotPassedNow() == true)
            return storedValue as T
        accessMutex.withLock {
            if (timeout?.hasNotPassedNow() == true)
                return storedValue as T
            val result: T = producer()
            storedValue = result
            timeout = TimeSource.Monotonic.markNow() + duration
            return result
        }
    }

    @Suppress("UNCHECKED_CAST")
    //use cast instead of !! because T might be nullable
    suspend fun getValue(maxAge: Duration): T {
        accessMutex.withLock {
            if (age <= maxAge) return storedValue as T
            val result: T = producer()
            storedValue = result
            timeout = TimeSource.Monotonic.markNow() + duration
            return result
        }
    }

    suspend fun getStoredValue(): Pair<Boolean, T?> {
        if (timeout?.hasNotPassedNow() == true)
            return true to storedValue
        return accessMutex.withLock {
            if (timeout?.hasNotPassedNow() == true)
                true to storedValue
            else
                false to null
        }
    }

    suspend fun getStoredValue(maxAge: Duration): Pair<Boolean, T?> {
        return accessMutex.withLock {
            if (age <= maxAge)
                true to storedValue
            else
                false to null
        }
    }

    fun invalidateNow() {
        timeout = null
    }

    suspend fun invalidate() {
        accessMutex.withLock {
            timeout = null
        }
    }

    suspend fun updateValue(): T {
        accessMutex.withLock {
            val result: T = producer()
            storedValue = result
            timeout = TimeSource.Monotonic.markNow() + duration
            return result
        }
    }
}

fun <T> cachedValue(producer: suspend () -> T): CachedValue<T> = CachedValue(Duration.INFINITE, producer)
fun <T> cachedValue(default: T, producer: suspend () -> T): CachedValue<T> =
    CachedValue(Duration.INFINITE, producer, default, Duration.INFINITE)

fun <T> cachedValue(duration: Duration, producer: suspend () -> T): CachedValue<T> = CachedValue(duration, producer)

fun <T> cachedValue(
    duration: Duration,
    default: T,
    defaultDuration: Duration = duration,
    producer: suspend () -> T
): CachedValue<T> = CachedValue(duration, producer, default, defaultDuration)
