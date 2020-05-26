package de.robolab.client.jfx.style.components

import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

@Suppress("RedundantLambdaArrow")
fun Stylesheet.initInfoBarStyle() {
    MainStyle.infoBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderWidth = multi(box(0.px, 0.px, 0.px, 1.px))
        prefWidth = 20.em
    }

    MainStyle.codeArea {
        backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
    }


    Stylesheet.tableView {
        faintFocusColor = Color.TRANSPARENT
        focusColor = Color.TRANSPARENT
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())

        val c = MainStyle.theme.ui.borderColor.toFx().css

        Stylesheet.filler {
            backgroundColor = multi(Color.TRANSPARENT)
        }
        Stylesheet.columnHeader {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
            properties["-fx-table-cell-border-color"] = Unit to { _ -> c }
        }
        Stylesheet.columnHeaderBackground {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
            properties["-fx-table-cell-border-color"] = Unit to { _ -> c }
        }
        Stylesheet.tableRowCell {
            properties["-fx-table-cell-border-color"] = Unit to { _ -> c }

            Stylesheet.text {
                fill = MainStyle.theme.ui.primaryTextColor.toFx()
            }

            and(Stylesheet.odd) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
            }
            and(Stylesheet.even) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
            }
            and(Stylesheet.selected) {
                backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())
                Stylesheet.text {
                    fill = MainStyle.theme.ui.themeColor.toFx()
                }
            }
        }
    }
}
