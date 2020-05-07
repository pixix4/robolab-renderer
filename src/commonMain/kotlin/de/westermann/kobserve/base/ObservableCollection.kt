package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import kotlin.reflect.KProperty

interface ObservableCollection<out T> : ObservableValue<Collection<@UnsafeVariance T>>, Collection<T> {

    val onAdd: EventHandler<@UnsafeVariance T>
    val onRemove: EventHandler<@UnsafeVariance T>
    val onClear: EventHandler<Collection<@UnsafeVariance T>>

    override val size: Int
    override fun contains(element: @UnsafeVariance T): Boolean
    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean
    override fun isEmpty(): Boolean
    override fun iterator(): Iterator<T>

    override val value: Collection<T>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): Collection<T> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): Collection<T> {
        return this
    }
}
