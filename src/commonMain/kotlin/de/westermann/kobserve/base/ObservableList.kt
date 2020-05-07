package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.ObservableSubList
import de.westermann.kobserve.utils.ObservableListIterator
import kotlin.reflect.KProperty

interface ObservableList<out T> : ObservableCollection<T>, List<T> {

    val onAddIndex: EventHandler<AddEvent<@UnsafeVariance T>>
    val onSetIndex: EventHandler<SetEvent<@UnsafeVariance T>>
    val onRemoveIndex: EventHandler<RemoveEvent<@UnsafeVariance T>>

    override val onAdd: EventHandler<@UnsafeVariance T>
    override val onRemove: EventHandler<@UnsafeVariance T>
    override val onClear: EventHandler<Collection<@UnsafeVariance T>>

    override val size: Int
    override fun contains(element: @UnsafeVariance T): Boolean
    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean
    override fun isEmpty(): Boolean

    override fun get(index: Int): T
    override fun indexOf(element: @UnsafeVariance T): Int
    override fun lastIndexOf(element: @UnsafeVariance T): Int

    override val value: List<T>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): List<T> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): List<T> {
        return this
    }

    override fun subList(fromIndex: Int, toIndex: Int): ObservableList<T> {
        return ObservableSubList(this, fromIndex until toIndex)
    }

    override fun iterator(): Iterator<T> {
        return ObservableListIterator(this)
    }

    override fun listIterator(): ListIterator<T> {
        return ObservableListIterator(this)
    }

    override fun listIterator(index: Int): ListIterator<T> {
        return ObservableListIterator(this, index)
    }

    data class AddEvent<T>(
        val index: Int,
        val element: T
    )

    data class SetEvent<T>(
        val index: Int,
        val oldElement: T,
        val newElement: T
    )

    data class RemoveEvent<T>(
        val index: Int,
        val element: T
    )
}
