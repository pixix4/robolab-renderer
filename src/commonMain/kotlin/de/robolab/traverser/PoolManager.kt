package de.robolab.traverser

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.*

interface PoolManager<T> : ObservableReadOnlyList<T> {
    fun add(element: T)
    fun add(elements: Collection<T>) = elements.forEach(this::add)
    fun add(vararg elements: T) = elements.forEach(this::add)
    fun add(elements: Sequence<T>) = elements.forEach(this::add)

    fun tryRemove(): T?

    abstract class PoolManagerBase<T>(vararg elements: T) : PoolManager<T> {
        protected val pool: ObservableList<T> = observableListOf(*elements)

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
        override val onAdd: EventHandler<ListAddEvent<T>> = pool.onAdd
        override val onChange: EventHandler<Unit> = pool.onChange
        override val onRemove: EventHandler<ListRemoveEvent<T>> = pool.onRemove
        override val onUpdate: EventHandler<ListUpdateEvent<T>> = pool.onUpdate
        override val size: Int = pool.size

        override fun contains(element: T): Boolean = pool.contains(element)

        override fun containsAll(elements: Collection<T>): Boolean = pool.containsAll(elements)

        override fun get(index: Int): T = pool[index]

        override fun indexOf(element: T): Int = pool.indexOf(element)

        override fun lastIndexOf(element: T): Int = pool.lastIndexOf(element)
    }

    class FIFO<T>(vararg elements: T) : PoolManagerBase<T>(*elements) {

        override fun tryRemove(): T? = synchronized(pool) {
            if (pool.isEmpty())
                null
            else
                pool.removeAt(0)
        }

    }

    class LIFO<T>(vararg elements: T) : PoolManagerBase<T>(*elements) {

        override fun tryRemove(): T? = synchronized(pool) {
            if (pool.isEmpty())
                null
            else
                pool.removeAt(pool.lastIndex)
        }
    }
}