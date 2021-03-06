package de.robolab.common.utils

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.observableListOf

interface PoolManager<T> : ObservableList<T> {
    fun add(element: T)
    fun add(elements: Collection<T>) = elements.forEach {
        add(it)
    }

    fun add(vararg elements: T) = elements.forEach {
        add(it)
    }

    fun add(elements: Sequence<T>) = elements.forEach {
        add(it)
    }

    fun tryRemove(): T?

    abstract class PoolManagerBase<T>(vararg elements: T) : PoolManager<T> {
        protected val pool: ObservableMutableList<T> = observableListOf(*elements)

        override fun add(element: T) {
            pool.add(element)
        }

        override fun add(elements: Collection<T>) {
            pool.addAll(elements)
        }

        override fun add(vararg elements: T) {
            pool.addAll(elements)
        }

        override fun add(elements: Sequence<T>) {
            pool.addAll(elements)
        }

        override fun isEmpty(): Boolean = pool.isEmpty()
        override val onAddIndex: EventHandler<ObservableList.AddEvent<T>> = pool.onAddIndex
        override val onSetIndex: EventHandler<ObservableList.SetEvent<T>> = pool.onSetIndex
        override val onRemoveIndex: EventHandler<ObservableList.RemoveEvent<T>> = pool.onRemoveIndex
        override val onAdd: EventHandler<T> = pool.onAdd
        override val onRemove: EventHandler<T> = pool.onRemove
        override val onClear: EventHandler<Collection<T>> = pool.onClear
        override val onChange: EventHandler<Unit> = pool.onChange
        override val size: Int
            get() = pool.size

        override fun contains(element: T): Boolean = pool.contains(element)

        override fun containsAll(elements: Collection<T>): Boolean = pool.containsAll(elements)

        override fun get(index: Int): T = pool[index]

        override fun indexOf(element: T): Int = pool.indexOf(element)

        override fun lastIndexOf(element: T): Int = pool.lastIndexOf(element)
    }

    companion object {
        fun <T> create(vararg elements: T, lifo: Boolean): PoolManager<T> =
            if (lifo) LIFO(*elements) else FIFO(*elements)
    }

    class FIFO<T>(vararg elements: T) : PoolManagerBase<T>(*elements) {

        override fun tryRemove(): T? {
            return if (pool.isEmpty())
                null
            else
                pool.removeAt(0)
        }

    }

    class LIFO<T>(vararg elements: T) : PoolManagerBase<T>(*elements) {

        override fun tryRemove(): T? {
            return if (pool.isEmpty())
                null
            else
                pool.removeAt(pool.lastIndex)
        }
    }
}