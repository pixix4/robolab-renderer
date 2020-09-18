package de.robolab.client.ui.style.components

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.text.Font
import javafx.scene.text.FontSmoothingType
import tornadofx.*

fun Stylesheet.initMainCanvasStyle() {
    Stylesheet.dialogPane {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()

        MainStyle.iconView {
            fill = MainStyle.theme.ui.errorColor.toFx()
        }

        Stylesheet.headerPanel {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())

            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))
            borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
            padding = box(1.5.em, 1.5.em, 1.5.em, 1.em)

            Stylesheet.label {
                textFill = MainStyle.theme.ui.primaryTextColor.toFx()
            }
        }
        Stylesheet.content {
            backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
            textFill = MainStyle.theme.ui.primaryTextColor.toFx()

        }

        Stylesheet.buttonBar {
            backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        }
    }

    Stylesheet.textArea {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(1.px))
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        textFill = MainStyle.theme.ui.primaryTextColor.toFx()
        fontSmoothingType = FontSmoothingType.LCD
        font = Font.font("RobotoMono")

        Stylesheet.content {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
        }
    }
}