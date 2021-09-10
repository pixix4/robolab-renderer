package de.robolab.client.ui.views

import de.robolab.client.app.viewmodel.TerminalViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.adapter.toEvent
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.TextView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.sync

class TerminalView(
    private val viewModel: TerminalViewModel,
) : ViewCollection<View>() {

    init {
        val outputView = boxView("terminal-output")
        viewModel.onOutput { output ->
            outputView.boxView {
                for (line in output) {
                    textView(line)
                }
            }
        }

        val inputView = boxView("terminal-input")
        inputView.html.tabIndex = 0
        inputView.onKeyDown { event ->
            val e = event.toEvent()
            viewModel.onKeyDown(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()
            }
        }
        inputView.onKeyPress { event ->
            val e = event.toEvent()
            viewModel.onKeyPress(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()
            }
        }
        inputView.onKeyUp { event ->
            val e = event.toEvent()
            viewModel.onKeyUp(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()
            }
        }

        val inputContentView = inputView.boxView("terminal-input-content")
        inputView.textView(viewModel.suffixProperty) {
            classList += "terminal-input-suffix"
        }

        inputContentView.sync(
            viewModel.contentProperty,
            create = { item ->
                TextView(item.value).also { view ->
                    if (item.isCursor) {
                        view.classList += "cursor"
                    } else if (item.color != null) {
                        view.classList += item.color.name.lowercase()
                    }
                }
            },
            update = { view, item ->
                view.text = item.value

                view.classList.toggle("hidden", false)
                view.classList.toggle("cursor", false)
                for (c in ReplExecutor.HintColor.values()) {
                    view.classList.toggle(c.name.lowercase(), false)
                }

                if (item.isCursor) {
                    view.classList += "cursor"
                } else if (item.color != null) {
                    view.classList += item.color.name.lowercase()
                }
            },
            delete = { view ->
                view.text = ""
                view.classList.toggle("cursor", false)
                for (c in ReplExecutor.HintColor.values()) {
                    view.classList.toggle(c.name.lowercase(), false)
                }
                view.classList += "hidden"
            },
        )

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
