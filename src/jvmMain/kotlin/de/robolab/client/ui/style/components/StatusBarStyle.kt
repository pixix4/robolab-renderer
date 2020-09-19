package de.robolab.client.ui.style.components

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import tornadofx.*

fun Stylesheet.initStatusBarStyle() {
    MainStyle.statusBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))
        prefHeight = 2.em
        minHeight = 2.em
        maxHeight = 2.em
    }

    MainStyle.statusBarStatus {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderWidth = multi(box(0.px, 1.px, 0.px, 0.px))
    }

    MainStyle.statusBarBoxes {
        padding = box(0.2.em, 0.5.em)

        Stylesheet.label {
            padding = box(0.1.em, 0.6.em)
        }
    }

    Stylesheet.progressBar {
        prefHeight = 0.5.em
        minWidth = 8.em
        padding = box(0.px, 0.5.em)

        Stylesheet.track {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())

            borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
            borderRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundInsets = multi(box(0.px))
        }

        Stylesheet.bar {
            backgroundColor = multi(MainStyle.theme.ui.themeColor.toFx())

            borderWidth = multi(box(0.px))
            borderRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundInsets = multi(box(0.px))
            padding = box(1.px)
        }
    }

    MainStyle.memoryIndicator {
        Stylesheet.progressBar {
            prefHeight = 100.percent
            minWidth = 4.em
            padding = box(0.px)

            Stylesheet.track {
                backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
                borderWidth = multi(box(0.px))
                backgroundRadius = multi(box(0.px))
            }

            Stylesheet.bar {
                backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())

                borderRadius = multi(box(0.px))
                backgroundRadius = multi(box(0.px))
                padding = box(0.px)
            }
        }
    }
}
