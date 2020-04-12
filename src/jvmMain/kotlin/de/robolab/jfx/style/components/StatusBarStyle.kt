package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import tornadofx.*

fun Stylesheet.initStatusBarStyle() {
    MainStyle.statusBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.borderColor))
        borderWidth = multi(box(1.px))
        padding = box(0.2.em)
    }
}
