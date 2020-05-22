package de.robolab.client.theme

import de.robolab.common.utils.Color

object LightTheme : ITheme {

    override val ui = object : IThemeUi {

        override val primaryBackground = Color(251, 251, 251)
        override val primaryHoverBackground = Color(255, 255, 255)

        override val secondaryBackground = Color(238, 238, 238)
        override val secondaryHoverBackground = Color(245, 245, 245)

        override val tertiaryBackground = Color(224, 224, 224)
        override val tertiaryHoverBackground = Color(233, 233, 233)

        override val primaryTextColor = Color(51, 51, 51)
        override val secondaryTextColor = primaryTextColor.interpolate(primaryBackground, 0.5)

        override val themeColor = Color(192, 57, 43)
        override val themeHoverColor = Color(231, 76, 60)
        override val themePrimaryText = Color(255, 255, 255)
        override val themeSecondaryText = themePrimaryText.interpolate(themeColor, 0.8)

        override val borderColor = Color(208, 208, 208)

        override val successColor = Color(46, 204, 113)
        override val successTextColor = Color(255, 255, 255)

        override val warnColor = Color(243, 156, 18)
        override val warnTextColor = Color(255, 255, 255)

        override val errorColor = Color(231, 76, 60)
        override val errorTextColor = Color(255, 255, 255)
    }

    override val editor = object : IThemeEditor {

        // Blue
        override val editorKeywordColor = Color(0, 0, 255)

        // Yellow-Brown
        override val editorDirectionColor = Color(128, 103, 50)

        // Yellow-Green
        override val editorNumberColor = Color(9, 134, 88)

        // Green
        override val editorCommentColor = Color(0, 128, 0)

        // Orange-Brown
        override val editorStringColor = Color(163, 21, 21)

        // Red
        override val editorErrorColor = Color(231, 76, 60)

        // Background highlight
        override val editorSelectedLineColor = Color(214, 234, 255)
    }

    override val plotter = object : IThemePlotter {

        override val primaryBackgroundColor = ui.primaryBackground
        override val secondaryBackgroundColor = ui.secondaryBackground

        override val lineColor = ui.primaryTextColor

        override val gridColor = secondaryBackgroundColor.interpolate(lineColor, 0.15)
        override val gridTextColor = secondaryBackgroundColor.interpolate(lineColor, 0.4)

        override val redColor = Color(192, 57, 43)
        override val blueColor = Color(41, 128, 185)

        override val highlightColor = Color(243, 156, 18)
        override val editColor = Color(26, 188, 156)

        override val robotMainColor = Color(243, 156, 18)
        override val robotDisplayColor = Color(240, 240, 240)
        override val robotWheelColor = lineColor
        override val robotSensorColor = robotWheelColor.interpolate(robotMainColor, 0.1)
        override val robotButtonColor = Color(49, 31, 4)
    }

    override val traverser = object : IThemeTraverser {

        override val traverserCharacteristicCorrectColor = Color(0, 192, 0)
        override val traverserCharacteristicErrorColor = Color(192, 0, 0)
        override val traverserCharacteristicNorthColor = Color(128, 128, 0)
        override val traverserCharacteristicEastColor = Color(0, 128, 128)
        override val traverserCharacteristicSouthColor = Color(128, 0, 128)
        override val traverserCharacteristicWestColor = Color(0, 0, 192)
    }
}
