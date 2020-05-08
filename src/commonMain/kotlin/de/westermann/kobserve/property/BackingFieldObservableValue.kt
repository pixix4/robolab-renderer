package de.westermann.kobserve.property

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import kotlin.reflect.KProperty0

open class BackingFieldObservableValue<T>(
        private val attribute: KProperty0<T>
) : ObservableValue<T> {

    override val onChange = EventHandler<Unit>()

    protected open var internal: T = attribute.get()

    override fun get() = internal

    override fun invalidate() {
        val newValue = attribute.get()
        if (newValue != internal) {
            internal = newValue
            onChange.emit()
        }
    }
}

/**
 * Wrap this property in an ObservableValue. If the backing field changes, the invalidate() method needs to be called.
 */
fun <T> property(attribute: KProperty0<T>): ObservableValue<T> =
        BackingFieldObservableValue(attribute)

/**
 * Wrap this property in an ObservableValue. If the backing field changes, the invalidate() method needs to be called.
 */
fun <T> KProperty0<T>.observe(): ObservableValue<T> = BackingFieldObservableValue(this)
