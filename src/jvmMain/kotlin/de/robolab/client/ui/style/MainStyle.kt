package de.robolab.client.ui.style

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.components.*
import de.robolab.client.utils.PreferenceStorage
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class MainStyle : Stylesheet() {
    companion object {
        val buttonGroup by cssclass()
        val toolBar by cssclass()
        val toolBarContainer by cssclass()
        val iconView by cssclass()
        val listCellGraphic by cssclass()

        val navigationBar by cssclass()
        val statusBar by cssclass()
        val infoBar by cssclass()
        val detailBox by cssclass()
        val codeArea by cssclass()
        val dialog by cssclass()

        val success by cssclass()
        val warn by cssclass()
        val error by cssclass()

        val navigationBarBackButton by cssclass()

        val disabled by cssclass()
        val active by cssclass()

        val first by csspseudoclass()
        val last by csspseudoclass()

        val editorDefault by cssclass()
        val editorKeyword by cssclass()
        val editorDirection by cssclass()
        val editorNumber by cssclass()
        val editorComment by cssclass()
        val editorString by cssclass()
        val editorError by cssclass()
        val paragraphBox by cssclass()
        val hasCaret by csspseudoclass()

        var theme = PreferenceStorage.selectedTheme.theme

        val BORDER_RADIUS = 6.px

        fun updateTheme() {
            theme = PreferenceStorage.selectedTheme.theme
        }

        val monospaceFonts = listOf(
            "/RobotoMono/RobotoMono-Regular.ttf",
            "/RobotoMono/RobotoMono-Bold.ttf",
            "/RobotoMono/RobotoMono-Italic.ttf",
            "/RobotoMono/RobotoMono-BoldItalic.ttf"
        )

        val defaultFonts = listOf(
            "/Roboto/Roboto-Regular.ttf",
            "/Roboto/Roboto-Bold.ttf",
            "/Roboto/Roboto-Italic.ttf",
            "/Roboto/Roboto-BoldItalic.ttf"
        )
    }

    init {
        updateTheme()

        for (font in monospaceFonts) {
            loadFont(font, 12)
        }

        for (font in defaultFonts) {
            loadFont(font, 12)
        }

        star {
            faintFocusColor = Color.TRANSPARENT

            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))
        }
        root {
            textFill = theme.ui.primaryTextColor.toFx()
            backgroundColor = multi(theme.ui.secondaryBackground.toFx())
            font = Font.font("Roboto")
        }

        dialog {
            // Border should be provided by the os
            //borderStyle = multi(BorderStrokeStyle.SOLID)
            //borderWidth = multi(box(1.px))
            //borderColor = multi(box(theme.ui.borderColor.toFx()))

            maxHeight = 32.em
        }

        scrollPane {
            backgroundColor = multi(theme.ui.secondaryBackground.toFx())
            backgroundInsets = multi(box(0.px))

            viewport {
                backgroundColor = multi(theme.ui.secondaryBackground.toFx())
                backgroundInsets = multi(box(0.px))
            }
        }

        initCodeAreaStyle()
        initFormStyle()
        initInfoBarStyle()
        initMainCanvasStyle()
        initNavigationBarStyle()
        initStatusBarStyle()
        initToolBarStyle()

        // Debug generated css:
        // println(render().split("\n").mapIndexed { i, s -> "${i.toString().padStart(3, '0')}: $s" }.joinToString("\n"))
    }
}
