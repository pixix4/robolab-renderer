package de.robolab.jfx.style.components

import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.*

fun Stylesheet.initCodeAreaStyle() {

    MainStyle.codeArea {
        font = Font.font("RobotoMono")
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
