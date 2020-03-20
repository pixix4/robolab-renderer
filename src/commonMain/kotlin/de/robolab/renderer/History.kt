package de.robolab.renderer

import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class History<T : Any>(initValue: T) {

    private var historyIndexProperty = property(0)
    private var historyIndex by historyIndexProperty
    private var historyList = listOf<T>(initValue)

    private val readOnlyValueProperty = historyIndexProperty.mapBinding { historyList[it] }
    val valueProperty = property(object : FunctionAccessor<T> {
        override fun get(): T {
            return readOnlyValueProperty.get()
        }

        override fun set(value: T): Boolean {
            push(value)
            return true
        }
    }, readOnlyValueProperty)
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
                readOnlyValueProperty.invalidate()
            }
        } else {
            if (value == this.value) return
            historyList = historyList.take(historyIndex + 1) + value
            historyIndex += 1
        }
    }

    fun replace(value: T) {
        historyList = historyList.take(historyIndex) + value
        readOnlyValueProperty.invalidate()
    }
}
