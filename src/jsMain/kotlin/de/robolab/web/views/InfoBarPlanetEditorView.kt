package de.robolab.web.views

import de.robolab.app.model.file.InfoBarFileEditor
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.multilineInputView

fun BoxView.initInfoBarFileEditorView(content: InfoBarFileEditor) {
    multilineInputView(content.contentProperty).also {
        it.html.spellcheck = false
        it.html.autocomplete = "off"
        it.html.wrap = "off"
        it.html.setAttribute("autocapitalize", "none")
    }
}