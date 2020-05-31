package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableList.*
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit

open class ObservableSubList<T>(
        private val parent: ObservableList<T>,
        protected var range: IntRange
) : ObservableList<T> {

    override val onAddIndex = EventHandler<AddEvent<T>>()
    override val onSetIndex = EventHandler<SetEvent<T>>()
    override val onRemoveIndex = EventHandler<RemoveEvent<T>>()

    override val onAdd = EventHandler<T>()
    override val onRemove = EventHandler<T>()
    override val onClear = EventHandler<Collection<T>>()

    override val onChange = EventHandler<Unit>()

    private fun emitOnAdd(index: Int, element: T) {
        onAdd.emit(element)
        onAddIndex.emit(AddEvent(index, element))
        onChange.emit()
    }

    private fun emitOnSet(index: Int, oldElement: T, newElement: T) {
        onRemove.emit(oldElement)
        onAdd.emit(newElement)
        onSetIndex.emit(SetEvent(index, oldElement, newElement))
        onChange.emit()
    }

    private fun emitOnRemove(index: Int, element: T) {
        onRemove.emit(element)
        onRemoveIndex.emit(RemoveEvent(index, element))
        onChange.emit()
    }

    private fun emitOnClear(elements: Collection<T>) {
        if (elements.isEmpty()) return

        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = range.count()

    override fun contains(element: T): Boolean {
        for (elem in this) {
            if (elem == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun get(index: Int): T {
        if (index !in 0 until size) {
            throw IndexOutOfBoundsException()
        }

        return parent[index + range.first]
    }

    override fun indexOf(element: T): Int {
        return (0 until size).indexOfFirst { get(it) == element }
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun lastIndexOf(element: T): Int {
        return (0 until size).indexOfLast { get(it) == element }
    }

    override fun hashCode(): Int {
        var result = parent.hashCode()
        result = 31 * result + range.hashCode()
        return result
    }

    override fun toString(): String = joinToString(", ", "[", "]")

    init {
        parent.onAddIndex { (parentIndex, element) ->
            val index = parentIndex - range.first

            if (index in 0..size) {
                range = range.first..range.last + 1
                emitOnAdd(index, element)
            }
        }

        parent.onSetIndex { (parentIndex, oldElement, newElement) ->
            val index = parentIndex - range.first

            if (index in 0 until size) {
                emitOnSet(index, oldElement, newElement)
            }
        }

        parent.onRemoveIndex { (parentIndex, element) ->
            val index = parentIndex - range.first

            if (index in 0 until size) {
                range = range.first until range.last
                emitOnRemove(index, element)
            }
        }

        parent.onClear { elements ->
            if (elements is List<T>) {
                val e = elements.subList(range.first, range.last + 1)
                range = IntRange.EMPTY
                emitOnClear(e)
            } else {
                range = IntRange.EMPTY
                emitOnClear(elements)
            }
        }
    }
}
