package de.robolab.jfx.style

import de.robolab.jfx.style.components.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class MainStyle : Stylesheet() {
    companion object {
        val buttonGroup by cssclass()
        val textButton by cssclass()

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


        val primaryBackground = Color.web("#FBFBFB")
        val primaryHoverBackground = Color.web("#FFFFFF")

        val secondaryBackground = Color.web("#EEEEEE")
        val secondaryHoverBackground = Color.web("#F5F5F5")

        val tertiaryBackground = Color.web("#E0E0E0")
        val tertiaryHoverBackground = Color.web("#E9E9E9")

        val primaryTextColor = Color.web("#333333")
        val secondaryTextColor = Color.web("#888")

        val themeColor = Color.web("#c0392b")
        val themeText = Color.web("#FFFFFF")

        val borderColor = Color.web("#D0D0D0")

        val successColor = Color.web("#2ecc71")
        val successTextColor = Color.web("#FFFFFF")

        val warnColor = Color.web("#f1c40f")
        val warnTextColor = Color.web("#FFFFFF")

        val errorColor = Color.web("#e74c3c")
        val errorTextColor = Color.web("#FFFFFF")
        
        val BORDER_RADIUS = 6.px
    }

    init {
        loadFont("/Roboto/Roboto-Regular.ttf", 12)
        loadFont("/Roboto/Roboto-Bold.ttf", 12)
        loadFont("/Roboto/Roboto-Italic.ttf", 12)
        loadFont("/Roboto/Roboto-BoldItalic.ttf", 12)

        star {
            faintFocusColor = Color.TRANSPARENT

            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))

            //textFill = primaryTextColor
            //text {
            //    fill = primaryTextColor
            //}
        }
        root {
            textFill = primaryTextColor
            backgroundColor = multi(secondaryBackground)
            font = Font.font("Roboto")
        }

        initFormStyle()
        initInfoBarStyle()
        initMainCanvasStyle()
        initSideBarStyle()
        initStatusBarStyle()
        initToolBarStyle()
    }
}
