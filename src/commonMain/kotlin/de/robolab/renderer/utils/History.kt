package de.robolab.renderer.utils

import de.westermann.kobserve.Binding
import de.westermann.kobserve.Property
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class History<T : Any>(initValue: T) : Property<T> {

    private var historyIndexProperty = property(0)
    private var historyIndex by historyIndexProperty
    private var historyList = listOf(initValue)

    private val readOnlyValueProperty = historyIndexProperty.mapBinding { historyList[it] }

    override var binding: Binding<T> = Binding.Unbound()

    override fun get(): T {
        return readOnlyValueProperty.value
    }

    override fun set(value: T) {
        super.set(value)

        push(value)
    }

    override val onChange = EventHandler(readOnlyValueProperty.onChange)

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

    fun push(value: T) {
        if (value == this.value) return
        historyList = historyList.take(historyIndex + 1) + value
        historyIndex += 1

    }

    fun clear(value: T) {
        val lastIndex = historyIndex

        historyList = listOf(value)
        historyIndex = 0
        if (lastIndex == historyIndex) {
            readOnlyValueProperty.invalidate()
        }
    }

    fun replace(value: T) {
        historyList = historyList.take(historyIndex) + value
        readOnlyValueProperty.invalidate()
    }
}
