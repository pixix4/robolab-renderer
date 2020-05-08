package de.westermann.kobserve.property

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.emit
import kotlin.reflect.KProperty1

open class FlatMapObservableValue<R, T>(
        private val transform: (R) -> ObservableValue<T>,
        protected val receiver: ObservableValue<R>
) : ObservableValue<T> {

    final override val onChange = EventHandler<Unit>()

    private lateinit var reference: EventListener<Unit>

    override fun get(): T {
        return transform(receiver.value).value
    }

    private fun updateReference() {
        if (this::reference.isInitialized && reference.isAttached) {
            reference.detach()
        }

        reference = transform(receiver.value).onChange.reference {
            onChange.emit()
        }
    }

    init {
        receiver.onChange {
            updateReference()
            onChange.emit()
        }
        updateReference()
    }
}

fun <R, T> ObservableValue<R>.flatMapBinding(transform: (R) -> ObservableValue<T>): ObservableValue<T> =
        FlatMapObservableValue(transform, this)

fun <T> ObservableValue<ObservableValue<T>>.flattenBinding(): ObservableValue<T> =
        FlatMapObservableValue({ it }, this)

fun <R, T> ObservableValue<R>.flatMapBinding(attribute: KProperty1<R, ObservableValue<T>>): ObservableValue<T> =
        FlatMapObservableValue(attribute::get, this)
