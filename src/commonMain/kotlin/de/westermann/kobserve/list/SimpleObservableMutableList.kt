package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.event.emit

class SimpleObservableMutableList<T>(
        backingField: MutableList<T>
) : BaseObservableList<T>(backingField), ObservableMutableList<T> {

    override fun add(element: T): Boolean {
        val isAdded = backingField.add(element)
        if (isAdded) {
            emitOnAdd(size - 1, element)
        }
        return isAdded
    }

    override fun add(index: Int, element: T) {
        backingField.add(index, element)
        emitOnAdd(index, element)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val isAdded = backingField.addAll(index, elements)
        if (isAdded) {
            for (i in index until index + elements.size) {
                emitOnAdd(i, backingField[i], false)
            }
            onChange.emit()
        }
        return isAdded
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val isAdded = backingField.addAll(elements)
        if (isAdded) {
            for (i in size - elements.size until size) {
                emitOnAdd(i, backingField[i], false)
            }
            onChange.emit()
        }
        return isAdded
    }

    override fun clear() {
        val elements = backingField.toList()
        backingField.clear()
        emitOnClear(elements)
    }

    override fun atomicClearAndAdd(element: T) {
        val oldElements = backingField.toList()
        backingField.clear()
        val isAdded = backingField.add(element)

        emitOnClear(oldElements)
        if (isAdded) {
            emitOnAdd(size - 1, element)
        }
    }

    override fun atomicClearAndAddAll(elements: Collection<T>) {
        val oldElements = backingField.toList()
        backingField.clear()
        val isAdded = backingField.addAll(elements)

        emitOnClear(oldElements)
        if (isAdded) {
            for (i in size - elements.size until size) {
                emitOnAdd(i, backingField[i], false)
            }
            onChange.emit()
        }
    }

    override fun remove(element: T): Boolean {
        val index = backingField.indexOf(element)
        val isRemoved = backingField.remove(element)
        if (isRemoved) {
            emitOnRemove(index, element)
        }
        return isRemoved
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var isChanged = false

        var index = 0

        while (index < backingField.size) {
            if (backingField[index] !in elements) {
                index += 1
            } else {
                val elem = backingField.removeAt(index)
                isChanged = true
                emitOnRemove(index, elem, false)
            }
        }
        if (isChanged) {
            onChange.emit()
        }

        return isChanged
    }

    override fun removeAt(index: Int): T {
        val element = backingField.removeAt(index)
        emitOnRemove(index, element)
        return element
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var isChanged = false

        var index = 0

        while (index < backingField.size) {
            if (backingField[index] in elements) {
                index += 1
            } else {
                val elem = backingField.removeAt(index)
                isChanged = true
                emitOnRemove(index, elem, false)
            }
        }
        if (isChanged) {
            onChange.emit()
        }

        return isChanged
    }

    override fun set(index: Int, element: T): T {
        val oldElement = backingField.set(index, element)
        if (oldElement != element) {
            emitOnSet(index, oldElement, element)
        }
        return oldElement
    }
}

fun <T> listProperty(list: MutableList<T>): ObservableMutableList<T> = SimpleObservableMutableList(list)
fun <T> MutableList<T>.asObservable(): ObservableMutableList<T> = SimpleObservableMutableList(this)
fun <T> observableListOf(vararg elements: T): ObservableMutableList<T> =
        SimpleObservableMutableList(mutableListOf(*elements))
