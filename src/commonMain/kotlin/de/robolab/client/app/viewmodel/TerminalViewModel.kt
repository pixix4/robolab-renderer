package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.TerminalController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.repl.*
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.buildList
import de.robolab.common.utils.ConsoleGreeter
import de.westermann.kobserve.event.EventHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class TerminalViewModel(
    private val uiController: UiController
    ) : ViewModel {

    val inputViewModel = TerminalInputViewModel()

    val onOutput = EventHandler<Pair<TerminalInputViewModel?, List<String>>>()

    private fun execute(input: String) {
        GlobalScope.launch {
            val output = ReplExecutor.execute(input)
            onOutput.emit(TerminalInputViewModel(input, true) to output)
        }
    }

    val activeProperty = uiController.terminalEnabledProperty
    fun setTerminalHeight(height: Double) {
        uiController.setTerminalHeight(height)
    }

    fun closeTerminal() {
        activeProperty.value = false
    }

    fun printHello() {
        val list = buildList<String> {
            addAll(ConsoleGreeter.appLogo.split("\n"))
            add(ConsoleGreeter.appClientCreators)
        }
        onOutput.emit(null to list)
    }

    init {
        TerminalController.onExecute {
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
