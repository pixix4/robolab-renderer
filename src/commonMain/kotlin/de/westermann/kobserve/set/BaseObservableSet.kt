package de.westermann.kobserve.set

import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.utils.ObservableIterator

abstract class BaseObservableSet<T>(
    protected val backingField: MutableSet<T>
) : ObservableSet<T> {

    override val onAdd = EventHandler<T>()
    override val onRemove = EventHandler<T>()
    override val onClear = EventHandler<Collection<T>>()

    override val onChange = EventHandler<Unit>()

    protected fun emitOnAdd(element: T) {
        onAdd.emit(element)
        onChange.emit()
    }

    protected fun emitOnRemove(element: T) {
        onRemove.emit(element)
        onChange.emit()
    }

    protected fun emitOnClear(elements: Set<T>) {
        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = backingField.size

    override fun contains(element: T): Boolean {
        return backingField.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return backingField.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return backingField.isEmpty()
    }

    override fun toString(): String {
        return backingField.toString()
    }

    override fun iterator(): Iterator<T> {
        return ObservableIterator(backingField.iterator())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        
        if (other is BaseObservableSet<*>) {
            return backingField == other.backingField
        }

        return backingField == other
    }

    override fun hashCode(): Int {
        return backingField.hashCode()
    }
}