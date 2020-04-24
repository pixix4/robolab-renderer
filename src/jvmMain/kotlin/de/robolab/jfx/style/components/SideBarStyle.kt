package de.robolab.jfx.style.components

import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initSideBarStyle() {

    MainStyle.sideBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 1.px, 0.px, 0.px))
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
    }

    Stylesheet.listView {
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))

        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        backgroundInsets = multi(box(0.px))

        focusColor = Color.TRANSPARENT
        faintFocusColor = Color.TRANSPARENT
    }
    Stylesheet.listCell {
        backgroundColor = multi(Color.TRANSPARENT)
        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
            Stylesheet.label {
                textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            }
        }
        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
            Stylesheet.label {
                textFill = MainStyle.theme.ui.themeColor.toFx()
            }

            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
            }
        }
        MainStyle.disabled {
            Stylesheet.label {
                textFill = MainStyle.theme.ui.secondaryTextColor.toFx()
            }
        }

        and(Stylesheet.empty) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
            }
        }
    }

    MainStyle.success {
        backgroundColor = multi(MainStyle.theme.ui.successColor.toFx())
        Stylesheet.label {
            textFill = MainStyle.theme.ui.successTextColor.toFx()
        }
    }

    MainStyle.warn {
        backgroundColor = multi(MainStyle.theme.ui.warnColor.toFx())
        Stylesheet.label {
            textFill = MainStyle.theme.ui.warnTextColor.toFx()
        }
    }

    MainStyle.error {
        backgroundColor = multi(MainStyle.theme.ui.errorColor.toFx())
        Stylesheet.label {
            textFill = MainStyle.theme.ui.errorTextColor.toFx()
        }
    }

    MainStyle.sideBarBackButton {
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
        }
    }

    Stylesheet.scrollBar {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())

        Stylesheet.thumb {
            backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
            borderRadius = multi(box(1.em))
            backgroundRadius = multi(box(1.em))

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.primaryTextColor.toFx())
            }
        }
        and(Stylesheet.pressed) {
            Stylesheet.thumb {
                backgroundColor = multi(MainStyle.theme.ui.themeColor.toFx())
            }
        }

        Stylesheet.incrementButton {
            padding = box(0.px)
            shape = ""
        }
        Stylesheet.incrementArrow {
            padding = box(0.px)
            shape = ""
        }
        Stylesheet.decrementButton {
            padding = box(0.px)
            shape = ""
        }
        Stylesheet.decrementArrow {
            padding = box(0.px)
            shape = ""
        }

        and(Stylesheet.vertical) {
            prefWidth = 0.6.em
        }
        and(Stylesheet.horizontal) {
            prefHeight = 0.6.em
        }
    }

    Stylesheet.corner {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryHoverBackground.toFx())
    }
}
