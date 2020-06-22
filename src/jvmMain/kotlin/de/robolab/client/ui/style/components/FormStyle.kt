package de.robolab.client.ui.style.components

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
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
        prefHeight = 2.2.em

        Stylesheet.arrow {
            backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
        }

        Stylesheet.listCell {
            backgroundColor = multi(Color.TRANSPARENT)
            textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            borderRadius = multi(box(MainStyle.BORDER_RADIUS))
            backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
            padding = box(0.3.em, 0.7.em)
            borderWidth = multi(box(0.px))
            borderStyle = multi(BorderStrokeStyle.NONE)
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
            borderWidth = multi(box(0.px))
            borderStyle = multi(BorderStrokeStyle.NONE)

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
        promptTextFill = MainStyle.theme.ui.secondaryTextColor.toFx()
        backgroundInsets = multi(box(0.px))
        prefHeight = 2.2.em
        padding = box(0.3.em, 0.7.em)

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
        prefHeight = 2.2.em

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
        Stylesheet.button {
            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))

            borderWidth = multi(box(1.px, 0.px, 1.px, 1.px))

            and(MainStyle.first) {
                borderRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
                backgroundRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
            }
            and(MainStyle.last) {
                borderWidth = multi(box(1.px))
                borderRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                backgroundRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                and(MainStyle.first) {
                    borderRadius = multi(box(MainStyle.BORDER_RADIUS))
                    backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
                }
            }
            and(Stylesheet.focused) {
                borderWidth = multi(box(1.px))
            }
        }

        Stylesheet.textField {
            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))

            borderWidth = multi(box(1.px, 0.px, 1.px, 1.px))

            and(MainStyle.first) {
                borderRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
                backgroundRadius = multi(box(MainStyle.BORDER_RADIUS, 0.px, 0.px, MainStyle.BORDER_RADIUS))
            }
            and(MainStyle.last) {
                borderWidth = multi(box(1.px))
                borderRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                backgroundRadius = multi(box(0.px, MainStyle.BORDER_RADIUS, MainStyle.BORDER_RADIUS, 0.px))
                and(MainStyle.first) {
                    borderRadius = multi(box(MainStyle.BORDER_RADIUS))
                    backgroundRadius = multi(box(MainStyle.BORDER_RADIUS))
                }
            }
            and(Stylesheet.focused) {
                borderWidth = multi(box(1.px))
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
                shape = "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"
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

    Stylesheet.tabHeaderBackground {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
    }
    Stylesheet.tab {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 4.px, 0.px))
        borderColor = multi(box(MainStyle.theme.ui.secondaryBackground.toFx()))
        padding = box(0.2.em, 1.em, 0.em, 1.em)
        focusColor = Color.TRANSPARENT
        faintFocusColor = Color.TRANSPARENT

        and(Stylesheet.hover, Stylesheet.focused) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
            borderColor = multi(box(MainStyle.theme.ui.secondaryHoverBackground.toFx()))
        }

        and(Stylesheet.selected) {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
            borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))

            and(Stylesheet.hover, Stylesheet.focused) {
                backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
            }
        }
    }
    Stylesheet.tabDownButton {
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
        padding = box (2.px)

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryHoverBackground.toFx())
            textFill = MainStyle.theme.ui.primaryTextColor.toFx()
        }
        and(Stylesheet.focused) {
            borderColor = multi(box(MainStyle.theme.ui.themeColor.toFx()))
        }

        Stylesheet.arrow {
            backgroundColor = multi(MainStyle.theme.ui.secondaryTextColor.toFx())
        }
    }
}
