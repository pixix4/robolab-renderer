package de.robolab.jfx.style.components

import de.robolab.jfx.style.MainStyle
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*
import javax.swing.text.Style

fun Stylesheet.initInfoBarStyle() {
    MainStyle.infoBar {
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderColor = multi(box(MainStyle.theme.borderColor))
        borderWidth = multi(box(0.px, 0.px, 0.px, 1.px))
        prefWidth = 20.em
    }

    MainStyle.codeArea {
        backgroundColor = multi(MainStyle.theme.primaryBackground)
    }


    Stylesheet.tableView {
        faintFocusColor = Color.TRANSPARENT
        focusColor = Color.TRANSPARENT
        backgroundColor = multi(MainStyle.theme.secondaryBackground)

        val c = MainStyle.theme.borderColor.css

        Stylesheet.columnHeader {
            backgroundColor = multi(MainStyle.theme.tertiaryBackground)
            properties["-fx-table-cell-border-color"] = Unit to { _ -> c }
        }
        Stylesheet.columnHeaderBackground {
            backgroundColor = multi(MainStyle.theme.tertiaryBackground)
            properties["-fx-table-cell-border-color"] = Unit to { _ -> c }
        }
        Stylesheet.tableRowCell {
            properties["-fx-table-cell-border-color"] = Unit to { _ -> c }

            Stylesheet.text {
                fill = MainStyle.theme.primaryTextColor
            }

            and(Stylesheet.odd) {
                backgroundColor = multi(MainStyle.theme.secondaryHoverBackground)
            }
            and(Stylesheet.even) {
                backgroundColor = multi(MainStyle.theme.secondaryBackground)
            }
            and(Stylesheet.selected) {
                backgroundColor = multi(MainStyle.theme.primaryBackground)
                Stylesheet.text {
                    fill = MainStyle.theme.themeColor
                }
            }
        }
    }
}
