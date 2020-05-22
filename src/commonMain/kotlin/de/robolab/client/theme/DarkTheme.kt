package de.robolab.client.theme

import de.robolab.common.utils.Color

object DarkTheme : ITheme {

    override val ui = object : IThemeUi {

        override val primaryBackground = Color(13, 13, 13)
        override val primaryHoverBackground = Color(0, 0, 0)

        override val secondaryBackground = Color(48, 48, 48)
        override val secondaryHoverBackground = Color(36, 36, 36)

        override val tertiaryBackground = Color(77, 77, 77)
        override val tertiaryHoverBackground = Color(64, 64, 64)

        override val primaryTextColor = Color(255, 255, 255)
        override val secondaryTextColor = primaryTextColor.interpolate(primaryBackground, 0.5)

        override val themeColor = Color(231, 76, 60)
        override val themeHoverColor = Color(192, 57, 43)
        override val themePrimaryText = Color(255, 255, 255)
        override val themeSecondaryText = themePrimaryText.interpolate(themeColor, 0.8)

        override val borderColor = Color(85, 85, 85)

        override val successColor = Color(35, 154, 85)
        override val successTextColor = Color(255, 255, 255)

        override val warnColor = Color(229, 147, 17)
        override val warnTextColor = Color(255, 255, 255)

        override val errorColor = Color(178, 53, 40)
        override val errorTextColor = Color(255, 255, 255)
    }

    override val editor = object : IThemeEditor {

        // Blue
        override val editorKeywordColor = Color(86, 156, 214)

        // Yellow-Brown
        override val editorDirectionColor = Color(208, 208, 161)

        // Yellow-Green
        override val editorNumberColor = Color(181, 206, 168)

        // Green
        override val editorCommentColor = Color(100, 143, 80)

        // Orange-Brown
        override val editorStringColor = Color(206, 145, 120)

        // Red
        override val editorErrorColor = Color(178, 53, 40)

        // Background highlight
        override val editorSelectedLineColor = Color(52, 58, 64)
    }

    override val plotter = object : IThemePlotter {

        override val primaryBackgroundColor = ui.primaryBackground
        override val secondaryBackgroundColor = ui.secondaryBackground

        override val lineColor = ui.primaryTextColor

        override val gridColor = secondaryBackgroundColor.interpolate(lineColor, 0.15)
        override val gridTextColor = secondaryBackgroundColor.interpolate(lineColor, 0.4)

        override val redColor = Color(192, 57, 43)
        override val blueColor = Color(41, 128, 185)

        override val highlightColor = Color(243, 156, 18).interpolate(secondaryBackgroundColor, 0.4)
        override val editColor = Color(46, 204, 113)

        override val robotMainColor = Color(243, 156, 18)
        override val robotDisplayColor = Color(240, 240, 240)
        override val robotWheelColor = lineColor
        override val robotSensorColor = robotWheelColor.interpolate(robotMainColor, 0.1)
        override val robotButtonColor = Color(49, 31, 4)
    }

    override val traverser = LightTheme.traverser
}
