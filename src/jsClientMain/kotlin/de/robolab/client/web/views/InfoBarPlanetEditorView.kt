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

    editor.value = content.content
    content.contentProperty.onChange {
        editor.value = content.content
    }

    editor.addOnChangeListener {
        content.contentProperty.value = editor.value
    }

    infoBarActiveProperty.onChange {
        editor.refresh()
    }
    infoBarWidthProperty.onChange {
        editor.refresh()
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

    fun refresh()
}