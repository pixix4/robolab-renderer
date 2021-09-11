package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.TerminalController
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.renderer.utils.History
import de.robolab.client.repl.ReplExecutor
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class TerminalInputViewModel(
    value: String = "",
    readOnly: Boolean = false,
) : ViewModel {

    data class HintContent(
        val value: String,
        val color: ReplExecutor.HintColor?,
        val range: IntRange,
        val isCursor: Boolean = false,
    )

    data class State(
        val value: String,
        val cursor: Int = value.length,
    )

    data class AutoCompleteEntry(
        val value: String,
        val suffix: String,
        val description: String,
        val selected: Boolean,
    )

    private val commandHistory = History("")
    private var commandHistoryDirty = true

    private val stateProperty = property(State(value))
    private var state by stateProperty

    private val stateValueProperty = stateProperty.mapBinding { it.value }
    private val stateCursorProperty = stateProperty.mapBinding { it.cursor }

    val readOnlyProperty = property(readOnly)
    var readOnly by readOnlyProperty

    private val hintProperty = stateValueProperty.mapBinding {
        ReplExecutor.hint(it)
    }

    val contentProperty = hintProperty.join(stateCursorProperty) { hint, _ ->
        val list = de.robolab.client.repl.base.buildList<HintContent> {
            var lastSplit = 0

            for ((range, color) in hint.highlight) {
                add(HintContent(
                    hint.input.substring(lastSplit, range.first),
                    null,
                    lastSplit until range.first
                ))
                add(HintContent(
                    state.value.substring(range),
                    color,
                    range
                ))
                lastSplit = range.last + 1
            }

            add(HintContent(
                state.value.substring(lastSplit, state.value.length),
                null,
                lastSplit until state.value.length
            ))
        }.filter { it.value.isNotEmpty() }.toMutableList()

        val cursor = state.cursor
        val cursorElement = HintContent("", null, cursor..cursor, true)

        for (i in 0 until list.size) {
            val range = list[i].range
            if (cursor == range.first) {
                list.add(i, cursorElement)
                return@join list
            }

            if (cursor <= range.last) {
                val curr = list.removeAt(i)
                val firstRange = curr.range.first until cursor
                val secondRange = cursor..curr.range.last

                list.add(i, HintContent(curr.value.substring(secondRange), curr.color, secondRange))
                list.add(i, cursorElement)
                list.add(i, HintContent(curr.value.substring(firstRange), curr.color, firstRange))

                return@join list
            }
        }

        list.add(cursorElement)

        list
    }

    val autoCompleteProperty = property(emptyList<AutoCompleteEntry>())

    val suffixProperty = hintProperty.join(autoCompleteProperty) { hint, autoComplete ->
        if (autoComplete.isEmpty()) {
            hint.suffix
        } else {
            autoComplete.first { it.selected }.suffix
        }
    }

    fun paste(input: String) {
        val state = state
        val value =
            state.value.substring(0, state.cursor) + input + state.value.substring(state.cursor, state.value.length)
        this.state = State(value, state.cursor + 1)
    }

    val onPasteRequest = EventHandler<Unit>()

    fun onKeyDown(event: KeyEvent) {
        if (event.ctrlKey) {
            when (event.keyCode) {
                KeyCode.V -> {
                    onPasteRequest.emit()
                }
                else -> {
                    // Nothing todo
                }
            }
            return
        }

        val state = state
        event.stopPropagation()
        when (event.keyCode) {
            KeyCode.ENTER -> {
                val currentAutoComplete = autoCompleteProperty.value
                if (currentAutoComplete.isNotEmpty()) {
                    val selectedIndex = currentAutoComplete.first { it.selected }
                    this.state = State(state.value + selectedIndex.suffix  + " ")
                    autoCompleteProperty.value = emptyList()
                    return
                }

                TerminalController.execute(state.value)

                while (commandHistory.canRedo) {
                    commandHistory.redo()
                }
                if (commandHistoryDirty) {
                    commandHistory.replace(state.value)
                } else {
                    commandHistory.push(state.value)
                }
                commandHistoryDirty = false

                this.state = State("")
            }
            KeyCode.ESCAPE -> {
                autoCompleteProperty.value = emptyList()
            }
            KeyCode.TAB -> {
                val changed = if (event.shiftKey) {
                    autoCompletePrev(true)
                } else {
                    autoCompleteNext(true)
                }
                if (changed) {
                    return
                }

                autoComplete()
                return
            }
            KeyCode.BACKSPACE -> {
                if (state.cursor > 0) {
                    val value = state.value.substring(0, state.cursor - 1) + state.value.substring(state.cursor,
                        state.value.length)
                    this.state = State(value, state.cursor - 1)
                }
            }
            KeyCode.DELETE -> {
                if (state.cursor < state.value.length) {
                    val value = state.value.substring(0, state.cursor) + state.value.substring(state.cursor + 1,
                        state.value.length)
                    this.state = State(value, state.cursor)
                }
            }
            KeyCode.HOME -> {
                if (state.cursor > 0) {
                    this.state = state.copy(cursor = 0)
                }
            }
            KeyCode.ARROW_LEFT -> {
                if (autoCompletePrev()) {
                    return
                }

                if (state.cursor > 0) {
                    this.state = state.copy(cursor = state.cursor - 1)
                }
            }
            KeyCode.ARROW_RIGHT -> {
                if (autoCompleteNext()) {
                    return
                }

                if (state.cursor < state.value.length) {
                    this.state = state.copy(cursor = state.cursor + 1)
                }
            }
            KeyCode.END -> {
                if (state.cursor < state.value.length) {
                    this.state = state.copy(cursor = state.value.length)
                }
            }
            KeyCode.ARROW_UP -> {
                if (autoCompletePrev()) {
                    return
                }

                if (!commandHistory.canRedo) {
                    if (commandHistoryDirty) {
                        commandHistory.replace(state.value)
                    } else {
                        commandHistory.push(state.value)
                        commandHistoryDirty = true
                    }
                }
                if (commandHistory.canUndo) {
                    commandHistory.undo()
                }
                this.state = State(commandHistory.value)
            }
            KeyCode.ARROW_DOWN -> {
                if (autoCompleteNext()) {
                    return
                }

                if (commandHistory.canRedo) {
                    commandHistory.redo()
                }
                this.state = State(commandHistory.value)
            }
            else -> {
                var c = event.keyCode.char ?: return

                if (!event.shiftKey) {
                    c = c.lowercaseChar()
                }

                val value =
                    state.value.substring(0, state.cursor) + c + state.value.substring(state.cursor, state.value.length)
                this.state = State(value, state.cursor + 1)
            }
        }

        autoCompleteProperty.value = emptyList()
    }

    fun onKeyPress(event: KeyEvent) {
        event.stopPropagation()
    }

    fun onKeyUp(event: KeyEvent) {
        event.stopPropagation()
    }

    private fun autoCompleteNext(loop: Boolean = false): Boolean {
        val currentAutoComplete = autoCompleteProperty.value
        if (currentAutoComplete.isNotEmpty()) {
            val selectedIndex = currentAutoComplete.indexOfFirst { it.selected }

            if (selectedIndex < currentAutoComplete.lastIndex) {
                autoCompleteProperty.value = currentAutoComplete.mapIndexed { index, element ->
                    when (index) {
                        selectedIndex + 1 -> element.copy(selected = true)
                        selectedIndex -> element.copy(selected = false)
                        else -> element
                    }
                }
            } else if (loop) {
                autoCompleteProperty.value = currentAutoComplete.mapIndexed { index, element ->
                    when (index) {
                        0 -> element.copy(selected = true)
                        selectedIndex -> element.copy(selected = false)
                        else -> element
                    }
                }
            }

            return true
        }
        return false
    }

    private fun autoCompletePrev(loop: Boolean = false): Boolean {
        val currentAutoComplete = autoCompleteProperty.value
        if (currentAutoComplete.isNotEmpty()) {
            val selectedIndex = currentAutoComplete.indexOfFirst { it.selected }

            if (selectedIndex > 0) {
                autoCompleteProperty.value = currentAutoComplete.mapIndexed { index, element ->
                    when (index) {
                        selectedIndex - 1 -> element.copy(selected = true)
                        selectedIndex -> element.copy(selected = false)
                        else -> element
                    }
                }
            } else if (loop) {
                autoCompleteProperty.value = currentAutoComplete.mapIndexed { index, element ->
                    when (index) {
                        currentAutoComplete.lastIndex -> element.copy(selected = true)
                        selectedIndex -> element.copy(selected = false)
                        else -> element
                    }
                }
            }

            return true
        }
        return false
    }


    private fun autoComplete() {
        val input = state.value

        val autoComplete = ReplExecutor.autoComplete(input)

        if (autoComplete.isEmpty()) {
            return
        }

        if (autoComplete.size == 1) {
            state = State(input + autoComplete.first().suffix + " ")
        } else {
            autoCompleteProperty.value = autoComplete.mapIndexed { index, element ->
                AutoCompleteEntry(
                    element.name,
                    element.suffix,
                    element.description,
                    index == 0
                )
            }
        }
    }

    fun selectAutoComplete(textContent: String) {
        val input = state.value

        val currentAutoComplete = autoCompleteProperty.value
        if (currentAutoComplete.isNotEmpty()) {
            val selected = currentAutoComplete.find { it.value == textContent } ?: return

            state = State(input + selected.suffix  + " ")
            autoCompleteProperty.value = emptyList()
            return
        }
    }
}
