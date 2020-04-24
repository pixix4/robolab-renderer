package de.robolab.jfx.style.components

import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

fun Stylesheet.initFormStyle() {
    MainStyle.iconView {
        fill = MainStyle.theme.ui.primaryTextColor.toFx()
    }
    Stylesheet.comboBox {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderRadius = multi(box(MainStyle.BORDER_RADIUS))
        backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
        backgroundInsets = multi(box(0.px))

        Stylesheet.arrow {
            backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
        }

        Stylesheet.listCell {
            backgroundColor = multi(Color.TRANSPARENT)
            textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            borderRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
        }

        and(Stylesheet.focused) {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
            borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
            }
        }

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryHoverBackground.toFx())
        }
    }
    Stylesheet.comboBoxPopup {
        backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()

        Stylesheet.listView {
            borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
        }

        Stylesheet.listCell {
            backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
            textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            padding = box(0.5.em)

            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))

            and(Stylesheet.selected) {
                backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
                textFill = MainStyle.theme.ui.themeColor.toFx()
                fontWeight = FontWeight.BOLD

                and(Stylesheet.hover) {
                    backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
                }
            }

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
            }
        }
    }
    Stylesheet.textField {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderRadius = multi(box(MainStyle.BORDER_RADIUS))
        backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
        backgroundInsets = multi(box(0.px))

        and(Stylesheet.focused) {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
            borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))

            and(Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
            }
        }

        and(Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryHoverBackground.toFx())
        }
    }
    Stylesheet.button {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
        borderRadius = multi(box(MainStyle.BORDER_RADIUS))
        backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))

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

    Stylesheet.toggleButton {
        backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()

        borderRadius = multi(box(MainStyle.BORDER_RADIUS))
        backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))

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
        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
            textFill = MainStyle.theme.ui.themeColor.toFx()
            fontWeight = FontWeight.BOLD

            and(Stylesheet.focused, Stylesheet.hover) {
                textFill = MainStyle.theme.ui.themeColor.toFx()
                backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
            }
            MainStyle.iconView {
                fill = MainStyle.theme.ui.themeColor.toFx()
            }
        }
    }

    Stylesheet.fieldset {
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
    }
    Stylesheet.field {
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
    }
    Stylesheet.label {
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
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
            backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
            borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
            borderRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundInsets = multi(box(0.px))
        }

        and(Stylesheet.hover) {
            Stylesheet.box {
                backgroundColor = multi(MainStyle.theme.ui.tertiaryHoverBackground.toFx())
            }
        }

        and(Stylesheet.focused) {
            Stylesheet.box {
                borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))
            }
        }

        and(Stylesheet.selected) {
            Stylesheet.box {
                backgroundColor = multi(MainStyle.theme.ui.themeColor.toFx())
                borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))
            }

            Stylesheet.mark {
                backgroundColor = multi(MainStyle.theme.ui.themePrimaryText.toFx())
            }


            and(Stylesheet.hover) {
                Stylesheet.box {
                    backgroundColor = multi(MainStyle.theme.ui.themeHoverColor.toFx())
                }
            }

            and(Stylesheet.focused) {
                Stylesheet.box {
                    backgroundColor = multi(MainStyle.theme.ui.themeHoverColor.toFx())
                }
            }
        }
    }
}
