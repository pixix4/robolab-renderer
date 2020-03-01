package de.robolab.renderer.animation

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property
import de.westermann.kobserve.property.readOnly
import kotlin.math.max
import kotlin.math.min

open class GenericTransition<T>(
        initialValue: T,
        private val interpolate: (from:T , to: T, progress: Double) -> T
) {
    
    private var duration: Double = 0.0
    private var offset: Double = 0.0
    private var progress: Double = 0.0
    private var state: State = State.STOPPED

    private var fromValue = initialValue
    private var toValue = initialValue

    private val internalValue = property(initialValue)
    val valueProperty = internalValue.readOnly()
    val value by valueProperty
    
    val onFinish = EventHandler<Unit>()

    fun animate(targetValue: T, duration: Double, offset: Double = 0.0) {
        fromValue = value
        toValue = targetValue

        this.duration = duration
        this.offset = offset

        progress = 0.0
        state = State.RUNNING

        update(0.0)
    }
    
    fun resetValue(newValue: T) {
        fromValue = newValue
        toValue = newValue

        duration = 0.0
        offset = 0.0

        state = State.RUNNING

        update(0.0)
    }
    
    private fun ease(p: Double): Double = p * p / (p * p + (p - 1) * (p - 1))

    fun update(ms_offset: Double): Boolean {
        if (state != State.RUNNING) return false

        if (offset + duration == 0.0) {
            progress = 1.0
            state = State.STOPPED

            internalValue.value = toValue
            onFinish.emit(Unit)

            return true
        }

        val newProgress = max(0.0, min(1.0, progress + ms_offset / (duration + offset)))

        if (newProgress != progress) {
            if (newProgress >= 1.0) {
                progress = 1.0
                state = State.STOPPED

                internalValue.value = toValue
                onFinish.emit(Unit)
            } else {
                progress = newProgress

                val offsetPercent = offset / (duration + offset)
                val offsetProgress = if (progress < offsetPercent) {
                    0.0
                } else {
                    (progress - offsetPercent) / (1.0 - offsetPercent)
                }

                internalValue.value = interpolate(fromValue, toValue, ease(offsetProgress))
            }

            return true
        }

        return false
    }

    enum class State {
        RUNNING, STOPPED
    }
}
