package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.text.FontWeight
import tornadofx.*

fun Stylesheet.initFormStyle() {
    Stylesheet.comboBox {
        backgroundColor = multi(MainStyle.primaryBackground)
        borderColor = multi(box(MainStyle.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderRadius = multi(box(4.px))
        backgroundRadius = multi(box(4.px))
        prefHeight = 80.percent
        padding = box(0.px)
    }
    Stylesheet.textField {
        backgroundColor = multi(MainStyle.primaryBackground)
        borderColor = multi(box(MainStyle.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderRadius = multi(box(4.px))
        backgroundRadius = multi(box(4.px))

        and(Stylesheet.focused) {
            borderColor = multi(box(MainStyle.borderColor))
            effect = DropShadow(BlurType.GAUSSIAN, MainStyle.borderColor, 3.0, 3.0, 0.0, 0.0)
        }
    }
    Stylesheet.button {
        backgroundColor = multi(MainStyle.tertiaryBackground)
        borderColor = multi(box(MainStyle.borderColor))
        textFill = MainStyle.primaryTextColor
        borderRadius = multi(box(2.px))

        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.tertiaryHoverBackground)
            textFill = MainStyle.primaryTextColor
        }
    }

    Stylesheet.toggleButton {
        backgroundColor = multi(MainStyle.tertiaryBackground)
        borderColor = multi(box(MainStyle.borderColor))
        textFill = MainStyle.primaryTextColor

        borderRadius = multi(box(MainStyle.BORDER_RADIUS))
        backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))

        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.tertiaryHoverBackground)
            textFill = MainStyle.primaryTextColor
        }
        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.primaryBackground)
            textFill = MainStyle.themeColor
            fontWeight = FontWeight.BOLD

            and(Stylesheet.focused, Stylesheet.hover) {
                textFill = MainStyle.themeColor
                backgroundColor = multi(MainStyle.primaryHoverBackground)
            }
        }
    }


    MainStyle.buttonGroup {
        Stylesheet.toggleButton {
            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))

            borderWidth = multi(box(1.px, 1.px, 1.px, 0.px))

            and(MainStyle.first) {
                borderWidth = multi(box(1.px))
                borderRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
                backgroundRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
            }
            and(MainStyle.last) {
                borderRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                backgroundRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                and(MainStyle.first) {
                    borderRadius = multi(box(MainStyle.BORDER_RADIUS))
                    backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
                }
            }
        }
        Stylesheet.button {
            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))

            borderWidth = multi(box(1.px, 1.px, 1.px, 0.px))

            and(MainStyle.first) {
                borderWidth = multi(box(1.px))
                borderRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
                backgroundRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
            }
            and(MainStyle.last) {
                borderRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                backgroundRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                and(MainStyle.first) {
                    borderRadius = multi(box(MainStyle.BORDER_RADIUS))
                    backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
                }
            }
        }
    }

}
