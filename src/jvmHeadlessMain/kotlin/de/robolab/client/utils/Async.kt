package de.robolab.client.utils

import java.util.*

actual class TimeoutReference(private val timerTask: TimerTask) {
    actual fun cancel() {
        timerTask.cancel()
    }
}

actual fun runAsync(block: () -> Unit) {
    throw UnsupportedOperationException()
}

actual fun runAfterTimeout(timeout: Int, block: () -> Unit): TimeoutReference {
    return runAfterTimeout(timeout.toLong(), block)
}

actual fun runAfterTimeout(timeout: Long, block: () -> Unit): TimeoutReference {
    throw UnsupportedOperationException()
}

actual fun runAfterTimeoutInterval(interval: Int, block: () -> Unit): TimeoutReference {
    return runAfterTimeoutInterval(interval.toLong(), block)
}

actual fun runAfterTimeoutInterval(interval: Long, block: () -> Unit): TimeoutReference {
    throw UnsupportedOperationException()
}
