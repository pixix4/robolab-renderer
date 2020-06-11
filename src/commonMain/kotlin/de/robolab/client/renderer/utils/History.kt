package de.robolab.client.renderer.utils

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class History<T : Any>(initValue: T) : ObservableProperty<T> {

    private val historyIndexProperty = property(0)
    private var historyIndex by historyIndexProperty
    private val historyListProperty = property(listOf(initValue))
    private var historyList by historyListProperty

    private val currentHistoryEntryProperty = historyIndexProperty.join(historyListProperty) { index, list -> list[index] }

    override var binding: Binding<T> = Binding.Unbound()

    override fun get(): T {
        return currentHistoryEntryProperty.value
    }

    override fun set(value: T) {
        super.set(value)

        push(value)
    }

    override val onChange = EventHandler(currentHistoryEntryProperty.onChange)

    val canUndoProperty = historyIndexProperty.mapBinding { index -> index > 0 }
    val canUndo by canUndoProperty

    val canRedoProperty = historyIndexProperty.join(historyListProperty) { index, list -> index < list.lastIndex }
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

    fun clear(value: T = historyList.last()) {
        historyIndex = 0
        historyList = listOf(value)
    }

    fun replace(value: T) {
        historyList = historyList.take(historyIndex) + value
    }
}
