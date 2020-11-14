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


    suspend fun getValue(): T {
        if (timeout?.hasNotPassedNow() == true)
            return storedValue!!
        accessMutex.withLock {
            if (timeout?.hasNotPassedNow() == true)
                return storedValue!!
            val result: T = producer()
            storedValue = result
            timeout = TimeSource.Monotonic.markNow() + duration
            return result
        }
    }

    suspend fun getValue(maxAge: Duration): T {
        accessMutex.withLock {
            if (age <= maxAge) return storedValue!!
            val result: T = producer()
            storedValue = result
            timeout = TimeSource.Monotonic.markNow() + duration
            return result
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
    producer: suspend () -> T,
    default: T,
    defaultDuration: Duration = duration
): CachedValue<T> = CachedValue(duration, producer, default, defaultDuration)
