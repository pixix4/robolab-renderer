package de.robolab.renderer

import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class History<T : Any>(initValue: T) {

    private var historyIndexProperty = property(0)
    private var historyIndex by historyIndexProperty
    private var historyList = listOf<T>(initValue)

    val valueProperty = property(object : FunctionAccessor<T> {
        override fun get(): T {
            return historyList[historyIndex]
        }

        override fun set(value: T): Boolean {
            push(value)
            return true
        }
    }, historyIndexProperty)
    val value by valueProperty

    fun undo() {
        if (historyIndex > 0) {
            historyIndex -= 1
        }
    }

    fun redo() {
        if (historyIndex < historyList.lastIndex) {
            historyIndex += 1
        }
    }

    fun push(value: T, reset: Boolean = false) {
        val lastIndex = historyIndex
        if (reset) {
            historyList = listOf(value)
            historyIndex = 0
            if (lastIndex == historyIndex) {
                valueProperty.invalidate()
            }
        } else {
            historyList = historyList.take(historyIndex + 1) + value
            historyIndex += 1
        }
    }
}
