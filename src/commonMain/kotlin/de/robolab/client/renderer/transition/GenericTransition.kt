package de.robolab.client.renderer.transition

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.listenTo
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min

open class GenericTransition<T>(
        initialValue: T,
        private val interpolate: (from: T, to: T, progress: Double) -> T
) : IAnimatable, ObservableProperty<T> {

    private var fromValue = initialValue
    val sourceValue: T
        get() = fromValue

    private val transitionList = mutableListOf<TransitionHelper>()

    val targetValue: T
        get() = transitionList.lastOrNull()?.toValue ?: fromValue

    private val internalValue = property(initialValue)
    final override val onChange = EventHandler<Unit>()

    override var binding: Binding<T>
        get() = internalValue.binding
        set(value) {
            internalValue.binding = value
        }

    override fun get() = internalValue.value

    override fun set(value: T) {
        internalValue.set(value)
    }

    init {
        onChange.listenTo(internalValue.onChange)
    }
    
    override val isRunning: Boolean
    get() = transitionList.isNotEmpty()

    override val onAnimationStart = EventHandler<Unit>()
    override val onAnimationFinish = EventHandler<Unit>()

    fun animate(targetValue: T, duration: Double, offset: Double = 0.0) {
        if (internalValue.isBound) return
        
        if (!isRunning && targetValue == this.targetValue) return

        transitionList += TransitionHelper(
                targetValue, duration, offset
        )
        
        if (transitionList.size == 1) {
            onAnimationStart.emit()
        }

        onUpdate(0.0)
    }

    fun resetValue(newValue: T) {
        if (internalValue.isBound) return

        fromValue = newValue
        internalValue.set(newValue)

        transitionList.clear()

        onUpdate(0.0)
    }

    override fun onUpdate(msOffset: Double): Boolean {
        if (!isRunning) {
            return false
        }

        val value = transitionList.fold(fromValue) { acc, transitionHelper ->
            transitionHelper.update(acc, msOffset)
        }

        while (transitionList.firstOrNull()?.isFinished == true) {
            fromValue = transitionList.removeAt(0).toValue
        }

        if (!isRunning) {
            onAnimationFinish.emit()
        }

        if (value != this.value) {
            internalValue.set(value)
            return true
        }

        return false
    }

    private inner class TransitionHelper(
            val toValue: T,
            duration: Double,
            private val offset: Double
    ) {
        private val total = duration + offset
        private var progress = if (total <= 0.0) 1.0 else 0.0

        val isFinished: Boolean
            get() = progress >= 1.0

        fun update(fromValue: T, ms_offset: Double): T {
            if (isFinished) {
                return toValue
            }

            progress = max(0.0, min(1.0, progress + ms_offset / total))

            if (isFinished) {
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
