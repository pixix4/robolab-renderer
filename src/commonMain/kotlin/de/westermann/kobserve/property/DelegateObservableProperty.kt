package de.westermann.kobserve.property

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

class FunctionProperty<T>(
        private val delegateAccessor: DelegatePropertyAccessor<T>
) : FunctionObservableValue<T>(delegateAccessor), ObservableProperty<T> {

    override var binding: Binding<T> = Binding.Unbound()

    override fun set(value: T) {
        delegateAccessor.set(value)
    }

    constructor(
            delegatePropertyAccessor: DelegatePropertyAccessor<T>,
            vararg properties: ObservableValue<*>
    ) : this(delegatePropertyAccessor) {
        listenTo(*properties)
    }
}

interface DelegatePropertyAccessor<T> : DelegateValueAccessor<T> {
    /**
     * Perform the set operation of a property.
     *
     * @param value The new value that should be applied.
     */
    fun set(value: T)
}

/**
 * Create a validation property that calls the given function accessor on every get and set operation.
 *
 * @param getter The get function.
 * @param setter The set function.
 * @param properties The new property will listen to their onChange events.
 */
fun <T> property(
        getter: () -> T,
        setter: (value: T) -> Unit,
        vararg properties: ObservableValue<*>
): ObservableProperty<T> = FunctionProperty(object : DelegatePropertyAccessor<T> {

    override fun get(): T = getter()

    override fun set(value: T) {
        setter(value)
    }
}, *properties)

/**
 * Create a validation property that calls the given function accessor on every get and set operation.
 *
 * @param delegatePropertyAccessor The function accessor to call.
 * @param properties The new property will listen to their onChange events.
 */
fun <T> property(
        delegatePropertyAccessor: DelegatePropertyAccessor<T>,
        vararg properties: ObservableValue<*>
): ObservableProperty<T> = FunctionProperty(delegatePropertyAccessor, *properties)
