package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

fun Stylesheet.initSideBarStyle() {
    Stylesheet.listView {
        borderColor = multi(box(MainStyle.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px, 0.px, 0.px, 0.px))

        backgroundColor = multi(MainStyle.secondaryBackground)
        backgroundInsets = multi(box(0.px))

        focusColor = Color.TRANSPARENT
        faintFocusColor = Color.TRANSPARENT
    }
    Stylesheet.listCell {
        backgroundColor = multi(Color.TRANSPARENT)
        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.secondaryHoverBackground)
            Stylesheet.label {
                textFill = MainStyle.primaryTextColor
            }
        }
        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.primaryBackground)
            Stylesheet.label {
                textFill = MainStyle.themeColor
            }

            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.primaryHoverBackground)
            }
        }
        MainStyle.disabled {
            Stylesheet.label {
                textFill = MainStyle.secondaryTextColor
            }
        }
    }

    MainStyle.success {
        backgroundColor = multi(MainStyle.successColor)
        Stylesheet.label {
            textFill = MainStyle.successTextColor
        }
    }

    MainStyle.warn {
        backgroundColor = multi(MainStyle.warnColor)
        Stylesheet.label {
            textFill = MainStyle.warnTextColor
        }
    }

    MainStyle.error {
        backgroundColor = multi(MainStyle.errorColor)
        Stylesheet.label {
            textFill = MainStyle.errorTextColor
        }
    }
}
