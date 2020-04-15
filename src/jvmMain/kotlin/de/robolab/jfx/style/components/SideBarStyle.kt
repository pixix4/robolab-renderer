package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initSideBarStyle() {
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
}
