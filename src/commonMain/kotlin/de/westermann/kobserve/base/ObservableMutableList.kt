package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.ObservableMutableSubList
import de.westermann.kobserve.utils.ObservableMutableListIterator
import kotlin.reflect.KProperty

interface ObservableMutableList<T> : ObservableMutableCollection<T>, ObservableList<T>, MutableList<T> {

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

    override val onAddIndex: EventHandler<ObservableList.AddEvent<T>>
    override val onSetIndex: EventHandler<ObservableList.SetEvent<T>>
    override val onRemoveIndex: EventHandler<ObservableList.RemoveEvent<T>>

    override val value: MutableList<T>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): MutableList<T> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): MutableList<T> {
        return this
    }

    override fun get(index: Int): T
    override fun indexOf(element: @UnsafeVariance T): Int
    override fun lastIndexOf(element: @UnsafeVariance T): Int
    override fun add(index: Int, element: T)
    override fun addAll(index: Int, elements: Collection<T>): Boolean
    override fun removeAt(index: Int): T
    override fun set(index: Int, element: T): T

    override fun subList(fromIndex: Int, toIndex: Int): ObservableMutableList<T> {
        return ObservableMutableSubList(this, fromIndex until toIndex)
    }

    override fun iterator(): MutableIterator<T> {
        return ObservableMutableListIterator(this)
    }

    override fun listIterator(): MutableListIterator<T> {
        return ObservableMutableListIterator(this)
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return ObservableMutableListIterator(this, index)
    }
}
