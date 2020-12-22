package de.robolab.client.utils

import kotlinx.browser.window

actual class TimeoutReference(private val handler: Int, private val type: Type) {
    actual fun cancel() {
        when (type) {
            Type.TIMEOUT -> window.clearTimeout(handler)
            Type.INTERVAL -> window.clearInterval(handler)
        }
    }

    enum class Type {
        TIMEOUT, INTERVAL
    }
}

actual fun runAsync(block: () -> Unit) {
    window.setTimeout(block, 1)
}

actual fun runAfterTimeout(timeout: Int, block: () -> Unit): TimeoutReference {
    return TimeoutReference(window.setTimeout(block, timeout), TimeoutReference.Type.TIMEOUT)
}

actual fun runAfterTimeout(timeout: Long, block: () -> Unit): TimeoutReference {
    return runAfterTimeout(timeout.toInt(), block)
}

actual fun runAfterTimeoutInterval(interval: Int, block: () -> Unit): TimeoutReference {
    return TimeoutReference(window.setInterval(block, interval), TimeoutReference.Type.INTERVAL)
}

actual fun runAfterTimeoutInterval(interval: Long, block: () -> Unit): TimeoutReference {
    return runAfterTimeoutInterval(interval.toInt(), block)
}

fun <T : Any> buildJsInterface(init: T.() -> Unit): T {
    @Suppress("UnsafeCastFromDynamic")
    val options: T = js("{}")
    init(options)
    return options
}
