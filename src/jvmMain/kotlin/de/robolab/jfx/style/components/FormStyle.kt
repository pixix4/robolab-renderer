package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

fun Stylesheet.initFormStyle() {
    MainStyle.iconView {
        fill = MainStyle.theme.primaryTextColor
    }
    Stylesheet.comboBox {
        backgroundColor = multi(MainStyle.theme.tertiaryBackground)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderRadius = multi(box(4.px))
        backgroundRadius = multi(box(4.px))
        textFill = MainStyle.theme.primaryTextColor
        //prefHeight = 80.percent
        //padding = box(0.px)

        Stylesheet.listCell {
            backgroundColor = multi(Color.TRANSPARENT)
            textFill = MainStyle.theme.primaryTextColor
            borderRadius = multi(box(4.px))
            backgroundRadius = multi(box(4.px))
        }

        and(Stylesheet.focused) {
            backgroundColor = multi(MainStyle.theme.primaryBackground)
            borderColor = multi(box(MainStyle.theme.themeColor))

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.primaryHoverBackground)
            }
        }

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.tertiaryHoverBackground)
        }
    }
    Stylesheet.comboBoxPopup {
        backgroundColor = multi(MainStyle.theme.primaryBackground)
        textFill = MainStyle.theme.primaryTextColor

        Stylesheet.listView {
            borderColor = multi(box(MainStyle.theme.borderColor))
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
        }

        Stylesheet.listCell {
            backgroundColor = multi(MainStyle.theme.secondaryBackground)
            textFill = MainStyle.theme.primaryTextColor
            padding = box(0.5.em)

            and(Stylesheet.selected) {
                backgroundColor = multi(MainStyle.theme.primaryBackground)
                textFill = MainStyle.theme.themeColor
                fontWeight = FontWeight.BOLD

                and(Stylesheet.hover) {
                    backgroundColor = multi(MainStyle.theme.primaryHoverBackground)
                }
            }

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.secondaryHoverBackground)
            }
        }
    }
    Stylesheet.textField {
        backgroundColor = multi(MainStyle.theme.tertiaryBackground)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderRadius = multi(box(4.px))
        backgroundRadius = multi(box(4.px))
        textFill = MainStyle.theme.primaryTextColor

        and(Stylesheet.focused) {
            backgroundColor = multi(MainStyle.theme.primaryBackground)
            borderColor = multi(box(MainStyle.theme.themeColor))

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.primaryHoverBackground)
            }
        }

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.tertiaryHoverBackground)
        }
    }
    Stylesheet.button {
        backgroundColor = multi(MainStyle.theme.tertiaryBackground)
        borderColor = multi(box(MainStyle.theme.borderColor))
        textFill = MainStyle.theme.primaryTextColor
        borderRadius = multi(box(2.px))

        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.tertiaryHoverBackground)
            textFill = MainStyle.theme.primaryTextColor
        }
    }

    Stylesheet.toggleButton {
        backgroundColor = multi(MainStyle.theme.tertiaryBackground)
        borderColor = multi(box(MainStyle.theme.borderColor))
        textFill = MainStyle.theme.primaryTextColor

        borderRadius = multi(box(MainStyle.BORDER_RADIUS))
        backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))

        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.tertiaryHoverBackground)
            textFill = MainStyle.theme.primaryTextColor
        }
        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.theme.primaryBackground)
            textFill = MainStyle.theme.themeColor
            fontWeight = FontWeight.BOLD

            and(Stylesheet.focused, Stylesheet.hover) {
                textFill = MainStyle.theme.themeColor
                backgroundColor = multi(MainStyle.theme.primaryHoverBackground)
            }
            MainStyle.iconView {
                fill = MainStyle.theme.themeColor
            }
        }
    }

    Stylesheet.fieldset {
        textFill = MainStyle.theme.primaryTextColor
    }
    Stylesheet.field {
        textFill = MainStyle.theme.primaryTextColor
    }
    Stylesheet.label {
        textFill = MainStyle.theme.primaryTextColor
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

    Stylesheet.checkBox {

        Stylesheet.box {
            backgroundColor = multi(MainStyle.theme.tertiaryBackground)
            borderColor = multi(box(MainStyle.theme.borderColor))
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
            borderRadius = multi(box(4.px))
            backgroundRadius = multi(box(4.px))
        }

        and(Stylesheet.hover) {
            Stylesheet.box {
                backgroundColor = multi(MainStyle.theme.tertiaryHoverBackground)
            }
        }

        and(Stylesheet.focused) {
            Stylesheet.box {
                borderColor = multi(box(MainStyle.theme.themeColor))
            }
        }

        and(Stylesheet.selected) {
            Stylesheet.box {
                borderColor = multi(box(MainStyle.theme.themeColor))
                backgroundColor = multi(MainStyle.theme.themeColor)
            }

            Stylesheet.mark {
                backgroundColor = multi(MainStyle.theme.themeText)
            }
        }
    }
}
