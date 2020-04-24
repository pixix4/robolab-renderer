package de.robolab.web.views

import de.robolab.app.model.file.InfoBarFileEditor
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.multilineInputView
import org.w3c.dom.HTMLElement

fun BoxView.initInfoBarFileEditorView(content: InfoBarFileEditor) {

    val editorContainer = boxView("text-editor-container") {

    }

    val editor = TextEditor(editorContainer.html)

    editor.value = content.content
    content.contentProperty.onChange {
        editor.value = content.content
    }

    editor.addOnChangeListener {
        content.contentProperty.value = editor.value
    }

    // multilineInputView(content.contentProperty).also {
    //     it.html.spellcheck = false
    //     it.html.autocomplete = "off"
    //     it.html.wrap = "off"
    //     it.html.setAttribute("autocapitalize", "none")
    // }
}

external class TextEditor(container: HTMLElement) {
    var value: String

    fun addOnChangeListener(callback: () -> Unit)
}