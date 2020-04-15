package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import tornadofx.*

fun Stylesheet.initStatusBarStyle() {
    MainStyle.statusBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderWidth = multi(box(1.px))
        padding = box(0.2.em, 0.5.em)
        prefHeight = 2.em

        Stylesheet.label {
            backgroundColor = multi(MainStyle.theme.primaryBackground)
            borderWidth = multi(box(1.px))
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderColor = multi(box(MainStyle.theme.borderColor))
            padding = box(0.1.em, 0.6.em)

            borderRadius = multi(box(1.em))
            backgroundRadius = multi(box(1.em))
        }
    }
}
