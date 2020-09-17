package de.robolab.client.ui.style.components

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import javafx.geometry.Pos
import javafx.scene.layout.BorderStrokeStyle
import tornadofx.*

fun Stylesheet.initTabBarStyle() {

    MainStyle.tabBar {
        backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 0.px, 1.px, 0.px))

        minHeight = 2.2.em
        maxHeight = 2.2.em
        prefHeight = 2.2.em
    }

    MainStyle.tabBarContainer {
    }

    MainStyle.tabBarTab {
        alignment = Pos.CENTER
        padding = box(0.em, 0.5.em)
        borderColor = multi(box(MainStyle.theme.ui.borderColor.toFx()))
        borderStyle = multi(BorderStrokeStyle.SOLID)
        borderWidth = multi(box(0.px, 1.px, 0.px, 0.px))
        borderInsets = multi(box(0.px))
        backgroundInsets = multi(box(0.px, 0.px, 1.px, 0.px))

        MainStyle.iconView {
            opacity = 0.3
            and(Stylesheet.focused, Stylesheet.hover) {
                opacity = 1.0
            }
        }

        and(MainStyle.last) {
            borderWidth = multi(box(0.px))
        }

        and(Stylesheet.focused, Stylesheet.hover) {
            backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
        }

        and(MainStyle.active) {
            backgroundColor = multi(MainStyle.theme.ui.primaryBackground.toFx())

            Stylesheet.label {
                textFill = MainStyle.theme.ui.themeColor.toFx()
            }

            and(Stylesheet.focused, Stylesheet.hover) {
                backgroundColor = multi(MainStyle.theme.ui.primaryHoverBackground.toFx())
            }
        }
    }
    MainStyle.tabBarTabIcon {
        and(Stylesheet.focused, Stylesheet.hover) {
            MainStyle.iconView {
                opacity = 1.0
            }
        }
    }

    MainStyle.tabBarContainer {
    }

    MainStyle.tabBarSide {
        MainStyle.tabBarTab {
            borderWidth = multi(box(0.px))
            maxWidth = 3.em

            MainStyle.iconView {
                opacity = 0.6
            }

            and(Stylesheet.focused) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
            }

            and(MainStyle.active) {
                backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())

                MainStyle.iconView {
                    opacity = 1.0
                    fill = MainStyle.theme.ui.themeColor.toFx()
                }

                and(Stylesheet.focused) {
                    backgroundColor = multi(MainStyle.theme.ui.secondaryBackground.toFx())
                }

                and(Stylesheet.hover) {
                    backgroundColor = multi(MainStyle.theme.ui.secondaryHoverBackground.toFx())
                }
            }
        }
    }
}
