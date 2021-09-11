package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.TerminalViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.ui.views.utils.ResizeView
import de.robolab.client.utils.runAfterTimeout
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
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

        viewModel.onOutput { (input, output) ->
            outputView.boxView("terminal-output-block") {
                if (input != null) {
                    +ViewFactoryRegistry.create(input)
                }
                for (line in output) {
                    textView(line)
                }
            }

            runAfterTimeout(1) {
                inputView.scrollIntoView()
            }
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

        viewModel.printHello()
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
