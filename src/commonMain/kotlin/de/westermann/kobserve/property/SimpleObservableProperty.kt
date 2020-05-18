package de.westermann.kobserve.property

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit

class SimpleObservableProperty<T>(initValue: T) : ObservableProperty<T> {

    override val onChange = EventHandler<Unit>()

    override var binding: Binding<T> = Binding.Unbound()

    private var internal: T = initValue

    override fun get(): T = internal

    override fun set(value: T) {
        super.set(value)
        if (internal != value) {
            internal = value
            onChange.emit()
        }
    }
}

/**
 * Create an observable property with the given initial value. The property stores the value internally.
 *
 * @param initValue The initial value of the property.
 */
fun <T> property(initValue: T): ObservableProperty<T> = SimpleObservableProperty(initValue)

/**
 * Create an observable property with the null as initial value. The property stores the value internally.
 */
fun <T: Any> property(): ObservableProperty<T?> = SimpleObservableProperty(null)

/**
 * Create an observable property with the given initial value. The property stores the value internally.
 *
 * @receiver The initial value of the property.
 */
fun <T> T.observe(): ObservableProperty<T> = SimpleObservableProperty(this)
