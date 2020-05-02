package de.robolab.jfx.style

import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.components.*
import de.robolab.utils.PreferenceStorage
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class MainStyle : Stylesheet() {
    companion object {
        val buttonGroup by cssclass()
        val toolBar by cssclass()
        val iconView by cssclass()

        val sideBar by cssclass()
        val statusBar by cssclass()
        val infoBar by cssclass()
        val codeArea by cssclass()
        val dialog by cssclass()

        val success by cssclass()
        val warn by cssclass()
        val error by cssclass()

        val sideBarBackButton by cssclass()

        val disabled by cssclass()
        val active by cssclass()

        val first by csspseudoclass()
        val last by csspseudoclass()

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
    }

    init {
        updateTheme()

        loadFont("/RobotoMono/RobotoMono-Regular.ttf", 12)
        loadFont("/RobotoMono/RobotoMono-Bold.ttf", 12)
        loadFont("/RobotoMono/RobotoMono-Italic.ttf", 12)
        loadFont("/RobotoMono/RobotoMono-BoldItalic.ttf", 12)

        loadFont("/Roboto/Roboto-Regular.ttf", 12)
        loadFont("/Roboto/Roboto-Bold.ttf", 12)
        loadFont("/Roboto/Roboto-Italic.ttf", 12)
        loadFont("/Roboto/Roboto-BoldItalic.ttf", 12)

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
            borderStyle = multi(BorderStrokeStyle.SOLID)
            borderWidth = multi(box(1.px))
            borderColor = multi(box(theme.ui.borderColor.toFx()))

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
        initSideBarStyle()
        initStatusBarStyle()
        initToolBarStyle()

        // Debug generated css:
        // println(render().split("\n").mapIndexed { i, s -> "${i.toString().padStart(3, '0')}: $s" }.joinToString("\n"))
    }
}
