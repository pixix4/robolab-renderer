package de.westermann.kobserve.property

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.emit
import kotlin.reflect.KMutableProperty0

class BackingFieldObservableProperty<T>(
        private val attribute: KMutableProperty0<T>
) : BackingFieldObservableValue<T>(attribute), ObservableProperty<T> {

    override var binding: Binding<T> = Binding.Unbound()

    override fun set(value: T) {
        super.set(value)
        if (internal != value) {
            internal = value
            attribute.set(value)
            onChange.emit()
        }
    }

    override fun invalidate() {
        super<BackingFieldObservableValue>.invalidate()
    }
}

/**
 * Wrap this property in an ObservableProperty. If the backing field changes, the invalidate() method needs to be called.
 */
fun <T> property(attribute: KMutableProperty0<T>): ObservableProperty<T> =
        BackingFieldObservableProperty(attribute)

/**
 * Wrap this property in an ObservableProperty. If the backing field changes, the invalidate() method needs to be called.
 */
fun <T> KMutableProperty0<T>.observe(): ObservableProperty<T> = BackingFieldObservableProperty(this)
