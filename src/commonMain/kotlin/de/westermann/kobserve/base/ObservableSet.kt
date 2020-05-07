package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import kotlin.reflect.KProperty

interface ObservableSet<out T> : ObservableCollection<T>, Set<T> {

    override val onAdd: EventHandler<@UnsafeVariance T>
    override val onRemove: EventHandler<@UnsafeVariance T>
    override val onClear: EventHandler<Collection<@UnsafeVariance T>>

    override val size: Int
    override fun contains(element: @UnsafeVariance T): Boolean
    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean
    override fun isEmpty(): Boolean
    override fun iterator(): Iterator<T>

    override val value: Set<T>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): Set<T> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): Set<T> {
        return this
    }
}
