package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableList.*
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import kotlin.math.min

abstract class RelationalObservableList<T>(
        protected val parent: ObservableList<T>
) : ObservableList<T> {

    override val onAddIndex = EventHandler<AddEvent<T>>()
    override val onSetIndex = EventHandler<SetEvent<T>>()
    override val onRemoveIndex = EventHandler<RemoveEvent<T>>()

    override val onAdd = EventHandler<T>()
    override val onRemove = EventHandler<T>()
    override val onClear = EventHandler<Collection<T>>()

    override val onChange = EventHandler<Unit>()

    protected fun emitOnAdd(index: Int, element: T) {
        onAdd.emit(element)
        onAddIndex.emit(AddEvent(index, element))
    }

    protected fun emitOnSet(index: Int, oldElement: T, newElement: T) {
        onRemove.emit(oldElement)
        onAdd.emit(newElement)
        onSetIndex.emit(SetEvent(index, oldElement, newElement))
    }

    protected fun emitOnRemove(index: Int, element: T) {
        onRemove.emit(element)
        onRemoveIndex.emit(RemoveEvent(index, element))
    }

    protected fun emitOnClear(elements: List<T>) {
        if (elements.isEmpty()) return

        onClear.emit(elements)
        onChange.emit()
    }

    protected val relation: MutableList<Relation<T>> = mutableListOf()

    abstract fun createRelation(): Sequence<Relation<T>>

    override fun invalidate() {
        val oldRelationList = relation.toList()
        relation.clear()
        relation.addAll(createRelation())
        val newRelationList = relation.toList()

        if (newRelationList.isEmpty()) {
            emitOnClear(oldRelationList.map { it.element })
            return
        }

        var hasChanged = false

        val minSize = min(newRelationList.size, oldRelationList.size)
        for (index in 0 until minSize) {
            val newRelation = newRelationList[index]
            val oldRelation = oldRelationList[index]
            if (oldRelation.element != newRelation.element) {
                emitOnSet(index, oldRelation.element, newRelation.element)
                hasChanged = true
            }
        }

        for (index in minSize until newRelationList.size) {
            val element = newRelationList[index]
            emitOnAdd(index, element.element)
            hasChanged = true
        }

        for (index in oldRelationList.lastIndex downTo minSize) {
            val relation = oldRelationList[index]
            emitOnRemove(index, relation.element)
            hasChanged = true
        }

        if (hasChanged) {
            onChange.emit()
        }
    }

    override val size: Int
        get() = relation.size

    override fun contains(element: T): Boolean {
        for (elem in iterator()) {
            if (elem == element) {
                return true
            }
        }
        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        val notFound = elements.toList().toMutableList()

        for (elem in iterator()) {
            notFound -= elem

            if (notFound.isEmpty()) {
                return true
            }
        }

        return false
    }

    override fun get(index: Int): T {
        return parent[relation[index].index]
    }

    override fun isEmpty(): Boolean = relation.isEmpty()

    override fun indexOf(element: T): Int {
        for (index in 0 until size) {
            if (get(index) == element) return index
        }

        return -1
    }

    override fun lastIndexOf(element: T): Int {
        for (index in lastIndex downTo 0) {
            if (get(index) == element) return index
        }

        return -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is List<*>) return false
        if (size != other.size) return false

        for (index in 0 until size) {
            if (this[index] != other[index]) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = parent.hashCode()
        result = 31 * result + relation.hashCode()
        return result
    }

    override fun toString(): String = joinToString(", ", "[", "]")

    data class Relation<T>(var index: Int, var element: T)
}
