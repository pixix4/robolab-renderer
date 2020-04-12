package de.robolab.traverser

import kotlin.math.max

private fun forceNotNegative(value: Int): Int = max(value, -value - 1)

class PriorityQueue<T>(val comparator: Comparator<T>) : Collection<T> {

    private val store: MutableList<T> = mutableListOf()

    override val size: Int
        get() = store.size

    override fun contains(element: T): Boolean = equalityAssuringBinarySearch(element) >= 0

    override fun containsAll(elements: Collection<T>): Boolean = elements.all(::contains)

    override fun isEmpty(): Boolean = store.isEmpty()

    override fun iterator(): Iterator<T> = store.iterator()

    fun add(value: T) {
        val targetIndex: Int = forceNotNegative(equalityAssuringBinarySearch(value))
        store.add(targetIndex, value)
    }

    fun remove(): T = store.removeAt(0)

    fun remove(value: T): Boolean {
        val index: Int = equalityAssuringBinarySearch(value)
        if (index < 0) return false
        store.removeAt(index)
        return true
    }

    fun increasePriority(value: T, incrementer: () -> Unit): Boolean {
        val originalIndex: Int = equalityAssuringBinarySearch(value)
        incrementer()
        if (originalIndex < 0) return false
        val targetIndex: Int = forceNotNegative(store.binarySearch(value, comparator, 0, originalIndex))
        store.removeAt(originalIndex)
        store.add(targetIndex, value)
        return true
    }

    fun increasePriorityOrAdd(value: T, incrementer: () -> Unit) {
        val originalIndex: Int = equalityAssuringBinarySearch(value)
        incrementer()
        val targetIndex: Int = forceNotNegative(store.binarySearch(value, comparator, 0, originalIndex))
        if (originalIndex >= 0)
            store.removeAt(originalIndex)
        store.add(targetIndex, value)
    }

    private fun equalityAssuringBinarySearch(value: T, start: Int = 0, end: Int = store.size): Int {
        val index: Int = store.binarySearch(value, comparator, start, end)
        if (index < 0) return index
        var testIndex: Int = index
        while ((--testIndex) >= start) {
            val testValue: T = store[testIndex]
            if (testValue == value)
                return testIndex
            if (comparator.compare(testValue, value) != 0)
                break
        }
        testIndex = index
        while ((++testIndex) < end) {
            val testValue: T = store[testIndex]
            if (testValue == value)
                return testIndex
            if (comparator.compare(testValue, value) != 0)
                break
        }
        return -index - 1
    }
}