package de.westermann.kobserve.property

import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener

open class FlatMapReadNullableOnlyProperty<R, T>(
        private val transform: (R) -> ReadOnlyProperty<T>?,
        protected val receiver: ReadOnlyProperty<R>
) : ReadOnlyProperty<T?> {

    override fun get(): T? {
        return transform(receiver.value)?.value
    }

    final override val onChange = EventHandler<Unit>()

    private var reference: EventListener<Unit>? = null

    private fun updateReference() {
        reference?.detach()

        reference = transform(receiver.value)?.onChange?.reference {
            onChange.emit(Unit)
        }
    }

    init {
        receiver.onChange {
            updateReference()
            onChange.emit(Unit)
        }
        updateReference()
    }
}

fun <R, T> ReadOnlyProperty<R>.flatMapReadOnlyNullableBinding(transform: (R) -> ReadOnlyProperty<T>?): ReadOnlyProperty<T?> =
        FlatMapReadNullableOnlyProperty(transform, this)

fun <T> ReadOnlyProperty<ReadOnlyProperty<T>?>.flattenNullable(): ReadOnlyProperty<T?> =
        FlatMapReadNullableOnlyProperty({ it }, this)

fun <T> ReadOnlyProperty<ReadOnlyProperty<T>?>.flattenReadOnlyNullable(): ReadOnlyProperty<T?> =
        FlatMapReadNullableOnlyProperty({ it }, this)
