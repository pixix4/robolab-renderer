package de.westermann.kobserve.set

import de.westermann.kobserve.base.ObservableMutableSet
import de.westermann.kobserve.utils.ObservableMutableIterator

class SimpleObservableMutableSet<T>(
        backingField: MutableSet<T>
) : BaseObservableSet<T>(backingField), ObservableMutableSet<T> {

    override fun add(element: T): Boolean {
        val isAdded = backingField.add(element)
        if (isAdded) {
            emitOnAdd(element)
        }
        return isAdded
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var isChanged = false

        for (element in elements) {
            if (add(element)) {
                isChanged = true
            }
        }

        return isChanged
    }

    override fun clear() {
        val elements = backingField.toSet()
        backingField.clear()
        emitOnClear(elements)
    }

    override fun remove(element: T): Boolean {
        val isRemoved = backingField.remove(element)
        if (isRemoved) {
            emitOnRemove(element)
        }
        return isRemoved
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var isChanged = false

        for (element in elements) {
            if (remove(element)) {
                isChanged = true
            }
        }

        return isChanged
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var isChanged = false

        val iterator = backingField.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element !in elements) {
                iterator.remove()
                emitOnRemove(element)
                isChanged = true
            }
        }

        return isChanged
    }

    override fun iterator(): MutableIterator<T> {
        return ObservableMutableIterator(backingField.iterator()) { element ->
            emitOnRemove(element)
        }
    }
}

fun <T> setProperty(set: MutableSet<T>): ObservableMutableSet<T> = SimpleObservableMutableSet(set)
fun <T> MutableSet<T>.asObservable(): ObservableMutableSet<T> = SimpleObservableMutableSet(this)
fun <T> observableSetOf(vararg elements: T): ObservableMutableSet<T> =
        SimpleObservableMutableSet(mutableSetOf(*elements))
