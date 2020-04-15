package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import tornadofx.*

fun Stylesheet.initToolBarStyle() {
    Stylesheet.toolBar {
        backgroundColor = multi(MainStyle.theme.secondaryBackground)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))
    }
    Stylesheet.menuBar {
        backgroundColor = multi(MainStyle.theme.secondaryBackground)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

        Stylesheet.menu {
            borderStyle = multi(BorderStrokeStyle.NONE)
            borderWidth = multi(box(0.px))
            and(Stylesheet.showing) {
                backgroundColor = multi(MainStyle.theme.tertiaryBackground)
            }
        }
    }
}
