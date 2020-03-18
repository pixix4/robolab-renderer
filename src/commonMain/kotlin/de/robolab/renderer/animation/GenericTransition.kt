package de.robolab.renderer.animation

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property
import de.westermann.kobserve.property.readOnly
import kotlin.math.max
import kotlin.math.min

open class GenericTransition<T>(
        initialValue: T,
        private val interpolate: (from: T, to: T, progress: Double) -> T
) {
    
    private var fromValue = initialValue
    val sourceValue: T
        get() = fromValue
    
    private val transitionList = mutableListOf<TransitionHelper>()
    
    val targetValue: T
        get() = transitionList.lastOrNull()?.toValue ?: fromValue

    private val internalValue = property(initialValue)
    val valueProperty = internalValue.readOnly()
    val value by valueProperty

    val onFinish = EventHandler<Unit>()

    fun animate(targetValue: T, duration: Double, offset: Double = 0.0) {
        transitionList += TransitionHelper(
                targetValue, duration, offset
        )

        update(0.0)
    }

    fun resetValue(newValue: T) {
        fromValue = newValue
        internalValue.set(newValue)

        transitionList.clear()

        update(0.0)
    }

    fun update(ms_offset: Double): Boolean {
        if (transitionList.isEmpty()) {
            return false
        }

        val value = transitionList.fold(fromValue) { acc, transitionHelper ->
            transitionHelper.update(acc, ms_offset)
        }

        while (transitionList.firstOrNull()?.isFinished == true) {
            fromValue = transitionList.removeAt(0).toValue
        }

        if (transitionList.isEmpty()) {
            onFinish.emit(Unit)
        }

        if (value != this.value) {
            internalValue.set(value)
            return true
        }

        return false
    }

    inner class TransitionHelper(
            val toValue: T,
            duration: Double,
            private val offset: Double
    ) {
        private val total = duration + offset
        private var progress = if (total <= 0.0) 1.0 else 0.0

        val isFinished: Boolean
            get() = progress >= 1.0

        fun update(fromValue: T, ms_offset: Double): T {
            if (progress >= 1.0) {
                return toValue
            }

            progress = max(0.0, min(1.0, progress + ms_offset / total))

            if (progress >= 1.0) {
                return toValue
            }

            val offsetPercent = offset / total
            val offsetProgress = if (progress < offsetPercent) {
                0.0
            } else {
                (progress - offsetPercent) / (1.0 - offsetPercent)
            }

            return interpolate(fromValue, toValue, ease(offsetProgress))
        }
    }

    companion object {
        private fun ease(p: Double): Double = p * p / (p * p + (p - 1) * (p - 1))
    }
}
