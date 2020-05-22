package de.robolab.client.utils


actual class TimeoutReference {
    actual fun cancel() {
        throw UnsupportedOperationException()
    }
}

actual fun runAsync(block: () -> Unit) {
    throw UnsupportedOperationException()
}

actual fun runAfterTimeout(timeout: Int, block: () -> Unit): TimeoutReference {
    throw UnsupportedOperationException()
}

actual fun runAfterTimeout(timeout: Long, block: () -> Unit): TimeoutReference {
    throw UnsupportedOperationException()
}

actual fun runAfterTimeoutInterval(interval: Int, block: () -> Unit): TimeoutReference {
    throw UnsupportedOperationException()
}

actual fun runAfterTimeoutInterval(interval: Long, block: () -> Unit): TimeoutReference {
    throw UnsupportedOperationException()
}

fun <T : Any> buildJsInterface(init: T.() -> Unit): T {
    @Suppress("UnsafeCastFromDynamic")
    val options: T = js("{}")
    init(options)
    return options
}
