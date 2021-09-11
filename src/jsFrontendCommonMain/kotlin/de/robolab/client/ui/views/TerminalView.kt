package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.TerminalInputViewModel
import de.robolab.client.app.viewmodel.TerminalViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplColor
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.ui.views.utils.ResizeView
import de.robolab.client.utils.runAfterTimeout
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView
import kotlinx.browser.window

class TerminalView(
    viewModel: TerminalViewModel,
) : ViewCollection<View>() {

    init {
        classList.bind("active", viewModel.activeProperty)

        +ResizeView("terminal-bar-resize") { position, size ->
            viewModel.setTerminalHeight(window.innerHeight - position.y - size.y)
        }

        val scrollBox = boxView("terminal-scroll-box")

        val outputView = scrollBox.boxView("terminal-output")
        val inputView = TerminalInputView(viewModel.inputViewModel)
        scrollBox += inputView

        iconView(MaterialIcon.CLOSE) {
            classList += "terminal-close-button"

            onClick {
                viewModel.closeTerminal()
            }
        }

        viewModel.setOutputGenerator { input ->
            val output = TerminalOutputBox(input, scrollBox)
            outputView += output
            output
        }

        viewModel.activeProperty.onChange {
            if (viewModel.activeProperty.value) {
                inputView.focus()
            }
        }

        onClick { event ->
            inputView.focus()
            event.stopPropagation()
            event.preventDefault()
        }
    }

    companion object : ViewFactory {

        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is TerminalViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return TerminalView(viewModel as TerminalViewModel)
        }
    }
}

class TerminalOutputBox(input: TerminalInputViewModel?, private val scrollBox: BoxView) : ViewCollection<View>(),
    IReplOutput {

    var lastLine: BoxView? = null

    override fun writeString(message: String, color: ReplColor?) {
        val lines = message.split('\n')

        val line = lines.first()

        getLine().textView(line) {
            if (color != null) {
                classList += "terminal-${color.name.lowercase()}"
            }
        }

        for (additionalLine in lines.drop(1)) {
            newLine().textView(additionalLine) {
                if (color != null) {
                    classList += "terminal-${color.name.lowercase()}"
                }
            }
        }
    }

    override fun writeIcon(icon: MaterialIcon, color: ReplColor?) {
        getLine().iconView(icon) {
            if (color != null) {
                classList += color.toClassName()
            }
        }
    }

    private fun getLine(): BoxView {
        return lastLine ?: newLine()
    }

    private fun newLine(): BoxView {
        val line =  boxView("terminal-output-line")
        lastLine = line
        runAfterTimeout(1) {
            scrollBox.scrollTo(top = scrollBox.scrollHeight)
        }
        return line
    }

    init {
        if (input != null) {
            +ViewFactoryRegistry.create(input)
        }

        runAfterTimeout(1) {
            scrollBox.scrollTo(top = scrollBox.scrollHeight)
        }
    }
}
