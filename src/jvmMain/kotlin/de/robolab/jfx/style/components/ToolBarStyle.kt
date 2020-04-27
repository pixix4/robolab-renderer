package de.robolab.jfx.style.components

import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initToolBarStyle() {
    Stylesheet.toolBar {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

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
    Stylesheet.menuBar {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

        Stylesheet.menu {
            borderStyle = multi(BorderStrokeStyle.NONE)
            borderWidth = multi(box(0.px))
            and(Stylesheet.showing) {
                backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
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
    }
}
