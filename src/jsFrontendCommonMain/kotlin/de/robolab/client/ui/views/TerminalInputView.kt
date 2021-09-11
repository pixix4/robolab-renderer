package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.TerminalInputViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.repl.base.ReplColor
import de.robolab.client.repl.toColor
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.adapter.toEvent
import de.westermann.kwebview.ClassList
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.TextView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.sync
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLSpanElement

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

                scrollIntoView()
            }
        }
        onKeyPress { event ->
            val e = event.toEvent()
            viewModel.onKeyPress(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()

                scrollIntoView()
            }
        }
        onKeyUp { event ->
            val e = event.toEvent()
            viewModel.onKeyUp(e)
            if (!e.bubbles) {
                event.preventDefault()
                event.stopPropagation()

                scrollIntoView()
            }
        }

        iconView(MaterialIcon.CHEVRON_RIGHT) {
            classList += "terminal-input-prefix"
        }
        val inputContentView = boxView("terminal-input-content")
        textView(viewModel.suffixProperty) {
            classList += "terminal-input-suffix"
        }

        boxView("terminal-input-auto-complete") {
            sync(
                viewModel.autoCompleteProperty,
                create = { item ->
                    TextView(item.value).also { view ->
                        view.dataset["description"] = item.description
                        view.classList.toggle("selected", item.selected)
                    }
                },
                update = { view, item ->
                    view.text = item.value
                    view.dataset["description"] = item.description
                    view.classList.toggle("selected", item.selected)
                },
                delete = { view ->
                    view.text = ""
                    view.dataset["description"] = ""
                    view.classList.toggle("selected", false)
                },
            )

            onClick { event ->
                val t = event.target
                if (t is HTMLSpanElement) {
                    viewModel.selectAutoComplete(t.textContent ?: "")
                }
            }
        }

        inputContentView.sync(
            viewModel.contentProperty,
            create = { item ->
                TextView(item.value).also { view ->
                    view.classList.toggleTerminalColor(item.color?.toColor())

                    if (item.isCursor) {
                        view.classList += "cursor"
                    }
                }
            },
            update = { view, item ->
                view.text = item.value

                view.classList.toggle("hidden", false)
                view.classList.toggle("cursor", item.isCursor)
                view.classList.toggleTerminalColor(item.color?.toColor())
            },
            delete = { view ->
                view.text = ""
                view.classList.toggle("cursor", false)
                view.classList.toggleTerminalColor(null)
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

fun ClassList.toggleTerminalColor(color: ReplColor?) {
    for (c in ReplColor.values()) {
        toggle(c.toClassName(), c == color)
    }
}

fun ReplColor.toClassName() = "terminal-${name.lowercase()}"
