package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import kotlin.reflect.KProperty

interface ObservableMutableCollection<T> : ObservableCollection<T>, MutableCollection<T> {

    override val size: Int
    override fun contains(element: @UnsafeVariance T): Boolean
    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean
    override fun isEmpty(): Boolean
    override fun add(element: T): Boolean
    override fun addAll(elements: Collection<T>): Boolean
    override fun clear()
    override fun remove(element: T): Boolean
    override fun removeAll(elements: Collection<T>): Boolean
    override fun retainAll(elements: Collection<T>): Boolean

    override val onAdd: EventHandler<T>
    override val onRemove: EventHandler<T>
    override val onClear: EventHandler<Collection<T>>
    override fun iterator(): MutableIterator<T>

    override val value: MutableCollection<T>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): MutableCollection<T> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): MutableCollection<T> {
        return this
    }
}
