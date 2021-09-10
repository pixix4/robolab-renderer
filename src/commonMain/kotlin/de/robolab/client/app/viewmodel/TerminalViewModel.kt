package de.robolab.client.app.viewmodel

import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.renderer.utils.History
import de.robolab.client.repl.*
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.buildList
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class TerminalViewModel : ViewModel {

    val inputViewModel = TerminalInputViewModel()

    val onOutput = EventHandler<Pair<TerminalInputViewModel, List<String>>>()

    private fun execute(input: String) {
        GlobalScope.launch {
            val output = ReplExecutor.execute(input)
            onOutput.emit(TerminalInputViewModel(input, true) to output)
        }
    }

    init {
        inputViewModel.onExecute {
            execute(it)
        }

        val windowCommand = ReplCommandNode("window", "Modify the current window state")
        ReplRootCommand += windowCommand
        windowCommand += ReplSimpleCommand("split-h", "Split the current window horizontally") {
            listOf("Split-h")
        }
        windowCommand += ReplSimpleCommand("split-v", "Split the current window vertically") {
            listOf("Split-v")
        }
        windowCommand += ReplParameterCommand(
            "layout",
            "Transform the current window to the specified layout",
            ReplCommandParameterDescriptor(
                LayoutConstraint,
                "constraint",
                false
            ),
            ReplCommandParameterDescriptor(
                LayoutConstraint,
                "param2",
                true
            )
        ) { parameters ->
            val layoutConstraint = parameters.first() as LayoutConstraint

            listOf(
                "Set layout to $layoutConstraint"
            )
        }
    }
}

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

    private val commandHistory = History("")
    private var commandHistoryDirty = true

    private val stateProperty = property(State(value))
    private var state by stateProperty

    private val stateValueProperty = stateProperty.mapBinding { it.value }
    private val stateCursorProperty = stateProperty.mapBinding { it.cursor }

    val onExecute = EventHandler<String>()

    val readOnlyProperty = property(readOnly)
    var readOnly by readOnlyProperty

    private val hintProperty = stateValueProperty.mapBinding {
        ReplExecutor.hint(it)
    }

    val contentProperty = hintProperty.join(stateCursorProperty) { hint, _ ->
        val list = buildList<HintContent> {
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

    val suffixProperty = hintProperty.mapBinding { hint ->
        hint.suffix
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
                onExecute.emit(state.value)

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
            KeyCode.TAB -> {
                autoComplete()
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
                if (state.cursor > 0) {
                    this.state = state.copy(cursor = state.cursor - 1)
                }
            }
            KeyCode.ARROW_RIGHT -> {
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
    }

    fun onKeyPress(event: KeyEvent) {
        event.stopPropagation()
    }

    fun onKeyUp(event: KeyEvent) {
        event.stopPropagation()
    }

    private fun autoComplete() {
        val input = state.value
        val autoComplete = ReplExecutor.autoComplete(input)

        if (autoComplete.isNotEmpty()) {
            state = State(input + autoComplete.first().suffix)
        }
    }
}

data class LayoutConstraint(
    val rows: Int,
    val cols: Int,
) : IReplCommandParameter {

    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

    override fun toToken(): String = "${rows}x$cols"

    companion object : IReplCommandParameterTypeDescriptor<LayoutConstraint> {
        override val klazz: KClass<LayoutConstraint> = LayoutConstraint::class
        override val name: String = "LayoutConstraint"
        override val description = "Specify the window layout"
        override val pattern = "<rows>x<cols>"
        override val example = listOf(
            LayoutConstraint(3, 2).toToken()
        )
        override val regex: Regex = """\d+x\d+""".toRegex()

        override fun fromToken(token: String): LayoutConstraint? {
            val (cols, rows) = token.split("x", limit = 2)
            return LayoutConstraint(
                cols.toIntOrNull() ?: return null,
                rows.toIntOrNull() ?: return null
            )
        }
    }
}
