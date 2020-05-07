package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableMutableList

class ObservableMutableSubList<T>(
    private val parent: ObservableMutableList<T>,
    range: IntRange
) : ObservableSubList<T>(parent, range), ObservableMutableList<T> {

    override fun add(element: T): Boolean {
        parent.add(range.last + 1, element)
        return true
    }

    override fun add(index: Int, element: T) {
        if (index !in 0 .. size) {
            throw IndexOutOfBoundsException()
        }

        parent.add(range.first + index, element)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        if (index !in 0 until size) {
            throw IndexOutOfBoundsException()
        }

        return parent.addAll(range.first + index, elements)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return parent.addAll( range.last + 1, elements)
    }

    override fun clear() {
        for (i in 0 until size) {
            parent.removeAt(range.first)
        }
    }

    override fun remove(element: T): Boolean {
        return parent.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return parent.removeAll(elements)
    }

    override fun removeAt(index: Int): T {
        if (index !in 0 until size) {
            throw IndexOutOfBoundsException()
        }

        return parent.removeAt(range.first + index)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        return parent.retainAll(elements)
    }

    override fun set(index: Int, element: T): T {
        if (index !in 0 until size) {
            throw IndexOutOfBoundsException()
        }

        return parent.set(range.first + index, element)
    }
}
