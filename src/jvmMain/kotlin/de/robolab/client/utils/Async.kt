package de.robolab.client.utils

import javafx.application.Platform
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

actual class TimeoutReference(private val timerTask: TimerTask) {
    actual fun cancel() {
        timerTask.cancel()
    }
}

actual fun runAsync(block: () -> Unit) {
    Platform.runLater(block)
}

actual fun runAfterTimeout(timeout: Int, block: () -> Unit): TimeoutReference {
    return runAfterTimeout(timeout.toLong(), block)
}

actual fun runAfterTimeout(timeout: Long, block: () -> Unit): TimeoutReference {
    return TimeoutReference(Timer().schedule(timeout) {
        Platform.runLater(block)
    })
}

actual fun runAfterTimeoutInterval(interval: Int, block: () -> Unit): TimeoutReference {
    return runAfterTimeoutInterval(interval.toLong(), block)
}

actual fun runAfterTimeoutInterval(interval: Long, block: () -> Unit): TimeoutReference {
    return TimeoutReference(Timer().scheduleAtFixedRate(interval, interval) {
        Platform.runLater(block)
    })
}
