package de.robolab.client.web.views

import de.robolab.client.app.model.file.InfoBarFileEditor
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import org.w3c.dom.HTMLElement

fun BoxView.initInfoBarFileEditorView(
    content: InfoBarFileEditor,
    infoBarActiveProperty: ObservableProperty<Boolean>,
    infoBarWidthProperty: ObservableProperty<Double>
) {

    val editorContainer = boxView("text-editor-container")

    val editor = TextEditor(editorContainer.html)

    var ignoreUpdate = true
    editor.value = content.content
    content.contentProperty.onChange {
        ignoreUpdate = true
        editor.value = content.content
        ignoreUpdate = false
    }

    editor.addOnChangeListener {
        ignoreUpdate = true
        content.contentProperty.value = editor.value
        ignoreUpdate = false
    }
    editor.addOnCursorListener { line, _ ->
        if (!ignoreUpdate) {
            content.selectLine(line)
        }
    }

    infoBarActiveProperty.onChange {
        editor.refresh()
    }
    infoBarWidthProperty.onChange {
        editor.refresh()
    }

    content.onSetLine { line ->
        if (!ignoreUpdate) {
            editor.setCursor(line, 0)
        }
    }
    ignoreUpdate = false
}

external class TextEditor(container: HTMLElement) {
    var value: String

    fun addOnChangeListener(callback: () -> Unit)
    fun addOnCursorListener(callback: (line: Int, ch: Int) -> Unit)

    fun setCursor(line: Int, ch: Int)

    fun refresh()
}
