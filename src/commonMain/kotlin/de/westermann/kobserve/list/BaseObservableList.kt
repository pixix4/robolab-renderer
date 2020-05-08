package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableList.*
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit

abstract class BaseObservableList<T>(
        protected val backingField: MutableList<T>
) : ObservableList<T> {

    override val onAddIndex = EventHandler<AddEvent<T>>()
    override val onSetIndex = EventHandler<SetEvent<T>>()
    override val onRemoveIndex = EventHandler<RemoveEvent<T>>()

    override val onAdd = EventHandler<T>()
    override val onRemove = EventHandler<T>()
    override val onClear = EventHandler<Collection<T>>()

    override val onChange = EventHandler<Unit>()

    protected fun emitOnAdd(index: Int, element: T, emitOnChange: Boolean = true) {
        onAdd.emit(element)
        onAddIndex.emit(AddEvent(index, element))

        if (emitOnChange) {
            onChange.emit()
        }
    }

    protected fun emitOnSet(index: Int, oldElement: T, newElement: T, emitOnChange: Boolean = true) {
        onRemove.emit(oldElement)
        onAdd.emit(newElement)
        onSetIndex.emit(SetEvent(index, oldElement, newElement))

        if (emitOnChange) {
            onChange.emit()
        }
    }

    protected fun emitOnRemove(index: Int, element: T, emitOnChange: Boolean = true) {
        onRemove.emit(element)
        onRemoveIndex.emit(RemoveEvent(index, element))

        if (emitOnChange) {
            onChange.emit()
        }
    }

    protected fun emitOnClear(elements: List<T>) {
        if (elements.isEmpty()) return

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

    override fun get(index: Int): T {
        return backingField[index]
    }

    override fun indexOf(element: T): Int {
        return backingField.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return backingField.isEmpty()
    }

    override fun lastIndexOf(element: T): Int {
        return backingField.lastIndexOf(element)
    }

    override fun equals(other: Any?): Boolean {
        return backingField.equals(other)
    }

    override fun hashCode(): Int {
        return backingField.hashCode()
    }

    override fun toString(): String = backingField.toString()
}
