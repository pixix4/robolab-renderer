package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.TerminalController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.*
import de.robolab.common.utils.ConsoleGreeter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TerminalViewModel(
    private val uiController: UiController,
) : ViewModel {

    val inputViewModel = TerminalInputViewModel()

    private var replOutputGenerator: (TerminalInputViewModel?) -> IReplOutput = {
        DummyReplOutput
    }

    private var replInputGenerator: () -> IReplInput = {
        DummyReplInput
    }

    private fun execute(command: String) {
        GlobalScope.launch {
            val input = replInputGenerator()
            val output = replOutputGenerator(TerminalInputViewModel(command, true))
            ReplExecutor.execute(command, input, output)
        }
    }

    val activeProperty = uiController.terminalEnabledProperty
    fun setTerminalHeight(height: Double) {
        uiController.setTerminalHeight(height)
    }

    fun closeTerminal() {
        activeProperty.value = false
    }

    fun setInputGenerator(generator: () -> IReplInput) {
        replInputGenerator = generator
    }

    fun setOutputGenerator(generator: (TerminalInputViewModel?) -> IReplOutput) {
        replOutputGenerator = generator

        val output = generator(null)

        output.writeln(ConsoleGreeter.appLogo)
        output.writeln(ConsoleGreeter.appClientCreators, ReplColor.GREY)
    }

    init {
        TerminalController.onExecute {
            execute(it)
        }
    }
}
