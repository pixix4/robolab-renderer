package de.robolab.client.ui.style.components

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

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
            unsafe("-fx-table-cell-border-color", c)
        }
        Stylesheet.columnHeaderBackground {
            backgroundColor = multi(MainStyle.theme.ui.tertiaryBackground.toFx())
            unsafe("-fx-table-cell-border-color", c)
        }
        Stylesheet.tableRowCell {
            unsafe("-fx-table-cell-border-color", c)

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

    Stylesheet.scrollPane {
        padding = box(0.px)
    }

    MainStyle.scrollBoxView {
        MainStyle.scrollBoxViewEntry {
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
            borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

            and(MainStyle.last) {
                borderWidth = multi(box(0.px))
            }
        }
    }
}