package de.robolab.client.jfx.style.components

import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initNavigationBarStyle() {

    MainStyle.navigationBar {
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
        padding = box(0.px)

        focusColor = Color.TRANSPARENT
        faintFocusColor = Color.TRANSPARENT
    }
    Stylesheet.listCell {
        backgroundColor = multi(Color.TRANSPARENT)
        padding = box(0.px)

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
            Stylesheet.label {
                textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            }
        }

        MainStyle.listCellGraphic {
            padding = box(0.5.em)
        }

        MainStyle.active {
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

    MainStyle.navigationBarBackButton {
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
        }
    }

    Stylesheet.scrollBar {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
        padding = box(0.px)
        borderWidth = multi(box(0.px))
        borderInsets = multi(box(0.px))
        backgroundInsets = multi(box(0.px))
        borderStyle = multi(BorderStrokeStyle.NONE)


        Stylesheet.thumb {
            backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.a(0.6).toFx())
            borderRadius = multi(box(0.em))
            backgroundRadius = multi(box(0.em))
            padding = box(0.px)
            borderWidth = multi(box(0.px))
            borderInsets = multi(box(0.px))
            backgroundInsets = multi(box(0.px))
            borderStyle = multi(BorderStrokeStyle.NONE)

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
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
            prefWidth = 0.5.em
        }
        and(Stylesheet.horizontal) {
            prefHeight = 0.5.em
        }
    }

    Stylesheet.corner {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
    }
}
