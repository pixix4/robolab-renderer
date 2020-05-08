package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import kotlin.reflect.KProperty

interface ObservableMutableSet<T> : ObservableMutableCollection<T>, ObservableSet<T>, MutableSet<T> {

    override val onAdd: EventHandler<@UnsafeVariance T>
    override val onRemove: EventHandler<@UnsafeVariance T>
    override val onClear: EventHandler<Collection<@UnsafeVariance T>>


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
    override fun iterator(): MutableIterator<T>

    override val value: MutableSet<T>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): MutableSet<T> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): MutableSet<T> {
        return this
    }
}
