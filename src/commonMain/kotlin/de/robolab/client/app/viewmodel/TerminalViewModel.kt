package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.TerminalController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.repl.DummyReplOutput
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.IReplOutput
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

    private fun execute(input: String) {
        GlobalScope.launch {
            val output = replOutputGenerator(TerminalInputViewModel(input, true))
            ReplExecutor.execute(input, output)
        }
    }

    val activeProperty = uiController.terminalEnabledProperty
    fun setTerminalHeight(height: Double) {
        uiController.setTerminalHeight(height)
    }

    fun closeTerminal() {
        activeProperty.value = false
    }

    fun setOutputGenerator(generator: (TerminalInputViewModel?) -> IReplOutput) {
        replOutputGenerator = generator

        val output = generator(null)

        output.writeln(ConsoleGreeter.appLogo)
        output.writeln(ConsoleGreeter.appClientCreators)
    }

    init {
        TerminalController.onExecute {
            execute(it)
        }
    }
}
