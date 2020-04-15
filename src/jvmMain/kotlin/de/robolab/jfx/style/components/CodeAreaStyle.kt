package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.text.FontWeight
import tornadofx.*

fun Stylesheet.initCodeAreaStyle() {

    MainStyle.editorKeyword {
        fill = MainStyle.theme.editorKeywordColor
        fontWeight = FontWeight.BOLD
    }
    MainStyle.editorDirection {
        fill = MainStyle.theme.editorDirectionColor
        fontWeight = FontWeight.BOLD
    }
    MainStyle.editorNumber {
        fill = MainStyle.theme.editorNumberColor
    }
    MainStyle.editorComment {
        fill = MainStyle.theme.editorCommentColor
    }
    MainStyle.editorString {
        fill = MainStyle.theme.editorStringColor
    }
    MainStyle.editorError {
        fill = MainStyle.theme.editorErrorColor
        fontWeight = FontWeight.BOLD
        underline = true
    }
    MainStyle.paragraphBox {
        and(MainStyle.hasCaret) {
            backgroundColor += MainStyle.theme.editorSelectedLineColor
        }
    }
}
