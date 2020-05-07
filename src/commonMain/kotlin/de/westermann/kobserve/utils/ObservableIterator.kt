package de.westermann.kobserve.utils

import de.westermann.kobserve.base.ObservableMutableList

open class ObservableIterator<T>(
    private val iterator: Iterator<T>
): Iterator<T> {

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): T {
        return iterator.next()
    }
}
class ObservableMutableIterator<T>(
    private val iterator: MutableIterator<T>,
    private val onRemove: (element: T) -> Unit
): ObservableIterator<T>(iterator), MutableIterator<T> {

    private var lastElement: T? = null
    
    override fun next(): T {
        val element = super.next()

        lastElement = element

        return element
    }

    override fun remove() {
        iterator.remove()

        @Suppress("UNCHECKED_CAST")
        onRemove(lastElement as T)
    }
}

open class ObservableListIterator<T>(
    private val list: List<T>,
    protected var nextIndexValue: Int = 0
) : ListIterator<T> {

    protected var lastIndex = -1

    override fun hasNext(): Boolean = nextIndexValue < list.size

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        lastIndex = nextIndexValue++
        return list[lastIndex]
    }

    override fun hasPrevious(): Boolean = nextIndexValue > 0

    override fun nextIndex(): Int = nextIndexValue

    override fun previous(): T {
        if (!hasPrevious()) throw NoSuchElementException()

        lastIndex = --nextIndexValue
        return list[lastIndex]
    }

    override fun previousIndex(): Int = nextIndexValue - 1
}

class ObservableMutableListIterator<T>(
        private val list: ObservableMutableList<T>,
        nextIndexValue: Int = 0
) : ObservableListIterator<T>(list, nextIndexValue), MutableListIterator<T> {

    override fun remove() {
        check(lastIndex != -1) { "Call next() or previous() before removing element from the iterator." }

        list.removeAt(lastIndex)
        nextIndexValue = lastIndex
        lastIndex = -1
    }

    override fun add(element: T) {
        list.add(nextIndexValue, element)
        nextIndexValue++
        lastIndex = -1
    }

    override fun set(element: T) {
        check(lastIndex != -1) { "Call next() or previous() before updating element value with the iterator." }
        list[lastIndex] = element
    }
}
