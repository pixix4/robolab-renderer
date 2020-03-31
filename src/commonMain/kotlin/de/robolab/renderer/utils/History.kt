package de.robolab.renderer.utils

import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class History<T : Any>(initValue: T) {

    private var historyIndexProperty = property(0)
    private var historyIndex by historyIndexProperty
    private var historyList = listOf(initValue)

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

    val canUndoProperty = readOnlyValueProperty.mapBinding { historyIndex > 0 }
    val canUndo by canUndoProperty

    val canRedoProperty = readOnlyValueProperty.mapBinding { historyIndex < historyList.lastIndex }
    val canRedo by canRedoProperty

    fun undo() {
        if (canUndo) {
            historyIndex -= 1
        }
    }

    fun redo() {
        if (canRedo) {
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