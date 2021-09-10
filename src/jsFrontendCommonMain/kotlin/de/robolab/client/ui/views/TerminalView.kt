package de.robolab.client.ui.views

import de.robolab.client.app.viewmodel.TerminalInputViewModel
import de.robolab.client.app.viewmodel.TerminalViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.ui.adapter.toEvent
import de.robolab.client.utils.runAfterTimeout
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.TextView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.sync
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

class TerminalInputView(
    private val viewModel: TerminalInputViewModel,
) : ViewCollection<View>() {

    init {
        classList.bind("readonly", viewModel.readOnlyProperty)
        html.tabIndex = 0
        onKeyDown { event ->
            val e = event.toEvent()
            viewModel.onKeyDown(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()
            }
        }
        onKeyPress { event ->
            val e = event.toEvent()
            viewModel.onKeyPress(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()
            }
        }
        onKeyUp { event ->
            val e = event.toEvent()
            viewModel.onKeyUp(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()
            }
        }

        textView("> ") {
            classList += "terminal-input-prefix"
        }
        val inputContentView = boxView("terminal-input-content")
        textView(viewModel.suffixProperty) {
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

        viewModel.onPasteRequest {
            GlobalScope.launch {
                val clipboard = window.navigator.clipboard.readText().await()
                viewModel.paste(clipboard)
            }
        }
    }

    companion object : ViewFactory {

        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is TerminalInputViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return TerminalInputView(viewModel as TerminalInputViewModel)
        }
    }
}

class TerminalView(
    viewModel: TerminalViewModel,
) : ViewCollection<View>() {

    init {
        val outputView = boxView("terminal-output")

        val inputView = TerminalInputView(viewModel.inputViewModel)
        this += inputView

        viewModel.onOutput { (input, output) ->
            outputView.boxView("terminal-output-block") {
                +ViewFactoryRegistry.create(input)
                for (line in output) {
                    textView(line)
                }
            }

            runAfterTimeout(1) {
                inputView.scrollIntoView()
            }
        }

        onClick { event ->
            if (document.activeElement != inputView.html) {
                inputView.focus()
                event.stopPropagation()
                event.preventDefault()
            }
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
