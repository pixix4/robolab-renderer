package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import tornadofx.*

fun Stylesheet.initInfoBarStyle() {
    MainStyle.infoBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderWidth = multi(box(0.px, 0.px, 0.px, 1.px))
        prefWidth = 20.em

        and(MainStyle.active) {
            prefWidth = 20.em
        }
    }

    MainStyle.codeArea {
        backgroundColor = multi(MainStyle.theme.primaryBackground)
    }
}
