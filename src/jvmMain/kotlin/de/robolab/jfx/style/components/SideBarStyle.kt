package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initSideBarStyle() {

    MainStyle.sideBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 1.px, 0.px, 0.px))
        borderColor = multi(box(MainStyle.theme.borderColor))
    }

    Stylesheet.listView {
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))

        backgroundColor = multi(MainStyle.theme.secondaryBackground)
        backgroundInsets = multi(box(0.px))

        focusColor = Color.TRANSPARENT
        faintFocusColor = Color.TRANSPARENT
    }
    Stylesheet.listCell {
        backgroundColor = multi(Color.TRANSPARENT)
        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.secondaryHoverBackground)
            Stylesheet.label {
                textFill = MainStyle.theme.primaryTextColor
            }
        }
        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.theme.primaryBackground)
            Stylesheet.label {
                textFill = MainStyle.theme.themeColor
            }

            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.primaryHoverBackground)
            }
        }
        MainStyle.disabled {
            Stylesheet.label {
                textFill = MainStyle.theme.secondaryTextColor
            }
        }

        and(Stylesheet.empty) {
            backgroundColor = multi(MainStyle.theme.secondaryBackground)
            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.secondaryBackground)
            }
        }
    }

    MainStyle.success {
        backgroundColor = multi(MainStyle.theme.successColor)
        Stylesheet.label {
            textFill = MainStyle.theme.successTextColor
        }
    }

    MainStyle.warn {
        backgroundColor = multi(MainStyle.theme.warnColor)
        Stylesheet.label {
            textFill = MainStyle.theme.warnTextColor
        }
    }

    MainStyle.error {
        backgroundColor = multi(MainStyle.theme.errorColor)
        Stylesheet.label {
            textFill = MainStyle.theme.errorTextColor
        }
    }

    MainStyle.sideBarBackButton {
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.secondaryHoverBackground)
        }
    }

    Stylesheet.scrollBar {
        backgroundColor = multi(MainStyle.theme.tertiaryBackground)

        Stylesheet.thumb {
            backgroundColor = multi(MainStyle.theme.secondaryTextColor)
            borderRadius = multi(box(1.em))
            backgroundRadius = multi(box(1.em))

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.primaryTextColor)
            }
        }
        and(Stylesheet.pressed) {
            Stylesheet.thumb {
                backgroundColor = multi(MainStyle.theme.themeColor)
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
        backgroundColor = multi(MainStyle.theme.tertiaryHoverBackground)
    }
}
