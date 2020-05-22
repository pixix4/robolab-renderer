package de.robolab.client.utils


expect class TimeoutReference {
    fun cancel()
}

expect fun runAsync(block: () -> Unit)

expect fun runAfterTimeout(timeout: Int, block: () -> Unit): TimeoutReference
expect fun runAfterTimeout(timeout: Long, block: () -> Unit): TimeoutReference

expect fun runAfterTimeoutInterval(interval: Int, block: () -> Unit): TimeoutReference
expect fun runAfterTimeoutInterval(interval: Long, block: () -> Unit): TimeoutReference
