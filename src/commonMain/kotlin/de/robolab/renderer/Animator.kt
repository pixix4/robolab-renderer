package de.robolab.renderer

import de.westermann.kobserve.event.EventHandler
import kotlin.math.max
import kotlin.math.min

class Animator(
        private var duration: Int,
        private var offset: Int = 0
) {

    private var isRunning = false
    private var from: Double = 0.0
    private var to: Double = 0.0

    var progress: Double = 0.0
        private set

    var current: Double = from
        private set
    var isFinished: Boolean = (duration + offset) == 0
        private set

    val onFinish = EventHandler<Unit>()

    fun animate(from: Double = this.from, to: Double = this.to, duration: Int = this.duration, offset: Int = this.offset): Animator {
        onFinish.clearListeners()

        this.from = from
        this.to = to
        this.duration = duration
        this.offset = offset

        progress = 0.0
        current = from
        isFinished = (duration + offset) == 0
        isRunning = !isFinished
        if (isFinished) {
            onFinish.emit(Unit)
        }

        return this
    }

    private fun ease(p: Double): Double = p * p / (p * p + (p - 1) * (p - 1))

    private fun calculate() {
        val offsetPercent = offset.toDouble() / (duration + offset)
        val offsetProgress = if (progress < offsetPercent) {
            0.0
        } else {
            (progress - offsetPercent) / (1.0 - offsetPercent)
        }
        current = from + (to - from) * ease(offsetProgress)

        val oldFinished = isFinished
        isFinished = progress >= 1.0

        if (!oldFinished && isFinished) {
            onFinish.emit(Unit)
            isRunning = false
        }
    }

    fun update(ms: Double): Boolean {
        if (!isRunning) return false

        val newProgress = max(0.0, min(1.0, progress + ms / (duration + offset).toDouble()))

        if (newProgress != progress) {
            progress = newProgress
            calculate()
            return true
        }

        return false
    }

    fun finish() {
        progress = 1.0
        calculate()
    }

    init {
        calculate()
    }
}
