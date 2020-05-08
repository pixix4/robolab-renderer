package de.westermann.kobserve

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener

/**
 * Represents the current binding state,
 */
sealed class Binding<T> {

    /**
     * Returns the current binding state.
     */
    val isBound: Boolean
        get() = this !is Unbound<T>

    abstract val isWritable: Boolean

    /**
     * Unbound all corresponding properties.
     */
    abstract fun unbind()

    /**
     * Check if the property is writeable.
     *
     * @throws IllegalStateException if the property is bound in readonly mode.
     */
    open fun checkWrite(value: T) {}

    /**
     * Create an event listener that listens to source and applies all changes to target
     */
    protected fun listen(
            source: ObservableValue<T>,
            target: ObservableProperty<T>
    ): EventListener<Unit> =
            source.onChange.reference {
                val newValue = source.value
                if (target.value != newValue) {
                    target.value = newValue
                }
            }

    /**
     * Represents an unbound property state.
     */
    class Unbound<T> : Binding<T>() {
        override val isWritable: Boolean = true
        override fun unbind() = throw IllegalStateException("Property is currently not bounded!")
    }

    /**
     * Represents an readonly binding state.
     */
    class ReadOnlyBinding<T>(property: ObservableProperty<T>, private val target: ObservableValue<T>) : Binding<T>() {

        override val isWritable: Boolean = false

        private val targetReference: EventListener<Unit>

        override fun unbind() {
            targetReference.detach()
        }

        override fun checkWrite(value: T) {
            if (value != target.value) {
                throw IllegalStateException("Property is bounded in readonly mode!")
            }
        }

        init {
            property.value = target.value

            targetReference = listen(target, property)
        }
    }

    /**
     * Represents a bidirectional binding state.
     */
    class BidirectionalBinding<T>(property: ObservableProperty<T>, private val target: ObservableProperty<T>) : Binding<T>() {

        override val isWritable: Boolean
            get() = target.isWritable

        private val propertyReference: EventListener<Unit>
        private val targetReference: EventListener<Unit>

        override fun unbind() {
            propertyReference.detach()
            targetReference.detach()
        }

        init {
            property.value = target.value

            propertyReference = listen(target, property)
            targetReference = listen(property, target)
        }
    }
}
