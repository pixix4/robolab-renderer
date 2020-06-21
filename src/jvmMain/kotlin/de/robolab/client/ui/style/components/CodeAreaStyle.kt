package de.robolab.client.ui.style.components

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.multi

fun Stylesheet.initCodeAreaStyle() {

    MainStyle.codeArea {
        font = Font.font("RobotoMono")

    }

    s(".virtualized-scroll-pane") {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
    }

    MainStyle.editorDefault {
        fill = MainStyle.theme.ui.primaryTextColor.toFx()
    }
    MainStyle.editorKeyword {
        fill = MainStyle.theme.editor.editorKeywordColor.toFx()
        fontWeight = FontWeight.BOLD
    }
    MainStyle.editorDirection {
        fill = MainStyle.theme.editor.editorDirectionColor.toFx()
        fontWeight = FontWeight.BOLD
    }
    MainStyle.editorNumber {
        fill = MainStyle.theme.editor.editorNumberColor.toFx()
    }
    MainStyle.editorComment {
        fill = MainStyle.theme.editor.editorCommentColor.toFx()
    }
    MainStyle.editorString {
        fill = MainStyle.theme.editor.editorStringColor.toFx()
    }
    MainStyle.editorError {
        fill = MainStyle.theme.editor.editorErrorColor.toFx()
        fontWeight = FontWeight.BOLD
        underline = true
    }
    MainStyle.paragraphBox {
        and(MainStyle.hasCaret) {
            backgroundColor += MainStyle.theme.editor.editorSelectedLineColor.toFx()
        }
    }
}
