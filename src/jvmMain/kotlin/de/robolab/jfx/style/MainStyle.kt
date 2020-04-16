package de.robolab.jfx.style

import de.robolab.jfx.style.components.*
import de.robolab.jfx.style.theme.DarkThemeFx
import de.robolab.jfx.style.theme.IThemeFx
import de.robolab.jfx.style.theme.LightThemeFx
import de.robolab.renderer.theme.Theme
import de.robolab.utils.PreferenceStorage
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class MainStyle : Stylesheet() {
    companion object {
        val buttonGroup by cssclass()
        val iconView by cssclass()

        val sideBar by cssclass()
        val statusBar by cssclass()
        val infoBar by cssclass()
        val codeArea by cssclass()

        val success by cssclass()
        val warn by cssclass()
        val error by cssclass()

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

        var theme: IThemeFx = loadTheme()

        val BORDER_RADIUS = 6.px

        private fun loadTheme() = when (PreferenceStorage.selectedTheme) {
            Theme.LIGHT -> LightThemeFx
            Theme.DARK -> DarkThemeFx
        }

        fun updateTheme() {
            theme = loadTheme()
        }
    }

    init {
        updateTheme()

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
            textFill = theme.primaryTextColor
            backgroundColor = multi(theme.secondaryBackground)
            font = Font.font("Roboto")
        }

        initCodeAreaStyle()
        initFormStyle()
        initInfoBarStyle()
        initMainCanvasStyle()
        initSideBarStyle()
        initStatusBarStyle()
        initToolBarStyle()
    }
}
