package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.TerminalInputViewModel
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplColor
import de.robolab.client.repl.base.ReplFileType
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.ui.triggerDownload
import de.robolab.client.utils.runAfterTimeout
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TerminalOutputBox(input: TerminalInputViewModel?, private val scrollBox: BoxView) : ViewCollection<View>(),
    IReplOutput {

    private var lastLine: BoxView? = null

    override fun writeString(message: String, color: ReplColor?) {
        val lines = message.split('\n')

        val line = lines.first()

        getLine().textView(line) {
            classList += "terminal-output-entry-text"
            if (color != null) {
                classList += "terminal-${color.name.lowercase()}"
            }
        }

        for (additionalLine in lines.drop(1)) {
            newLine().textView(additionalLine) {
                classList += "terminal-output-entry-text"
                if (color != null) {
                    classList += "terminal-${color.name.lowercase()}"
                }
            }
        }
    }

    override fun writeIcon(icon: MaterialIcon, color: ReplColor?) {
        getLine().iconView(icon) {
            classList += "terminal-output-entry-icon"
            if (color != null) {
                classList += color.toClassName()
            }
        }
    }

    override fun writeFile(name: String, type: ReplFileType, content: suspend () -> String) {
        getLine().button("Download file") {
            classList += "terminal-output-entry-file"

            onClick {
                GlobalScope.launch {
                    val fileContent = content()

                    triggerDownload(name, fileContent)
                }
            }
        }
    }

    override fun writeAction(name: String, action: suspend () -> Unit) {
        getLine().button(name) {
            classList += "terminal-output-entry-action"

            onClick {
                GlobalScope.launch {
                    action()
                }
            }
        }
    }

    override fun clearCurrentLine() {
        getLine().clear()
    }

    private fun getLine(): BoxView {
        return lastLine ?: newLine()
    }

    private fun newLine(): BoxView {
        val line = boxView("terminal-output-line")
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
