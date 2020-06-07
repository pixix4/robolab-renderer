package de.robolab.client.jfx.style.components

import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initToolBarStyle() {

    MainStyle.toolBar {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

        prefHeight = 3.2.em

        Stylesheet.button {
            maxWidth = Double.MAX_VALUE.px
        }
    }
    Stylesheet.toolBar {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

        prefHeight = 3.2.em

        Stylesheet.toolBarOverflowButton {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
            borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
            textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            borderRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))

            prefWidth = 2.em
            prefHeight = 2.em

            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
            backgroundInsets = multi(box(0.px))

            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.tertiaryHoverBackground.toFx())
                textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            }
            and(Stylesheet.focused) {
                borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))
            }
        }

        Stylesheet.arrow {
            backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
        }

        Stylesheet.contextMenu {
            Stylesheet.menuItem {
                and(Stylesheet.focused, Stylesheet.hover) {
                    backgroundColor = multi(Color.TRANSPARENT)
                }
            }
        }
    }

    Stylesheet.contextMenu {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))

        borderRadius = multi(box(0.px))
        backgroundRadius = multi(box(0.px))
        backgroundInsets = multi(box(0.px))

        Stylesheet.menuItem {
            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
            }

            Stylesheet.arrow {
                backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
            }
        }

        s(".check-menu-item") {
            unsafe("-fx-mark-color", MainStyle.theme.ui.primaryTextColor.toFx())
            unsafe("-fx-focused-mark-color", MainStyle.theme.ui.primaryTextColor.toFx())
        }
    }
}
