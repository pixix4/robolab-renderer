package de.westermann.kobserve.property

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.emit
import kotlin.reflect.KProperty1

open class NullableFlatMapObservableValue<R, T>(
    private val transform: (R) -> ObservableValue<T>?,
    private val receiver: ObservableValue<R?>
) : ObservableValue<T?> {

    final override val onChange = EventHandler<Unit>()

    private var reference: EventListener<Unit>? = null

    override fun get(): T? {
        return transform(receiver.value ?: return null)?.value
    }

    private fun updateReference() {
        if (reference?.isAttached == true) {
            reference?.detach()
        }

        reference = transform(receiver.value ?: return)?.onChange?.reference {
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

fun <R, T> ObservableValue<R>.nullableFlatMapBinding(transform: (R) -> ObservableValue<T>?): ObservableValue<T?> =
    NullableFlatMapObservableValue(transform, this)

fun <T> ObservableValue<ObservableValue<T>?>.nullableFlattenBinding(): ObservableValue<T?> =
    NullableFlatMapObservableValue({ it }, this)

fun <R: Any, T> ObservableValue<R?>.nullableFlatMapBinding(attribute: KProperty1<R, ObservableValue<T>>): ObservableValue<T?> =
    NullableFlatMapObservableValue<R?, T>({
        if (it == null) null else attribute.get(it)
    }, this)
