package de.robolab.client.theme

import de.robolab.client.theme.utils.ITheme
import de.robolab.client.theme.utils.IThemeEditor
import de.robolab.client.theme.utils.IThemePlotter
import de.robolab.client.theme.utils.IThemeUi
import de.robolab.common.utils.Color

object GruvboxLightTheme : ITheme {

    // ----------------------------------------------------------------------------
    // Light Mode
    // ----------------------------------------------------------------------------

    // Light Background
    private val gbBg0 = Color(251, 241, 199)
    private val gbBg0Hard = Color(249, 245, 215)
    private val gbBg0Soft = Color(242, 229, 188)
    private val gbBg1 = Color(235, 219, 178)
    private val gbBg2 = Color(213, 196, 161)
    private val gbBg3 = Color(189, 174, 147)
    private val gbBg4 = Color(168, 153, 132)

    // Light Foreground
    private val gbFg0 = Color(40, 40, 40)
    private val gbFg1 = Color(60, 56, 54)
    private val gbFg2 = Color(80, 73, 69)
    private val gbFg3 = Color(102, 92, 84)
    private val gbFg4 = Color(124, 111, 100)

    // Light Colors
    private val gbDarkRed = Color(204, 36, 29)
    private val gbDarkGreen = Color(152, 151, 26)
    private val gbDarkYellow = Color(215, 153, 33)
    private val gbDarkBlue = Color(69, 133, 136)
    private val gbDarkPurple = Color(177, 98, 134)
    private val gbDarkAqua = Color(104, 157, 106)
    private val gbDarkOrange = Color(214, 93, 14)
    private val gbDarkGray = Color(146, 131, 116)

    private val gbLightRed = Color(157, 0, 6)
    private val gbLightGreen = Color(121, 116, 14)
    private val gbLightYellow = Color(181, 118, 20)
    private val gbLightBlue = Color(7, 102, 120)
    private val gbLightPurple = Color(143, 63, 113)
    private val gbLightAqua = Color(66, 123, 88)
    private val gbLightOrange = Color(175, 58, 3)
    private val gbLightGray = Color(124, 111, 100)

    override val ui = object : IThemeUi {

        override val primaryBackground = gbBg0Hard
        override val primaryHoverBackground = gbBg0Hard.interpolate(Color.WHITE, 0.3)

        override val secondaryBackground = gbBg0Soft
        override val secondaryHoverBackground = gbBg0

        override val tertiaryBackground = gbBg2
        override val tertiaryHoverBackground = gbBg1

        override val primaryTextColor = gbFg1
        override val secondaryTextColor = gbDarkGray

        override val themeColor = gbLightRed
        override val themeHoverColor = gbDarkRed
        override val themePrimaryText = gbBg0Hard
        override val themeSecondaryText = gbBg1

        override val borderColor = gbBg3

        override val successColor = gbDarkGreen
        override val successTextColor = gbBg0Hard

        override val warnColor = gbDarkYellow
        override val warnTextColor = gbBg0Hard

        override val errorColor = gbDarkRed
        override val errorTextColor = gbBg0Hard
    }

    override val editor = object : IThemeEditor {

        // Blue
        override val editorKeywordColor = gbLightBlue

        // Yellow-Brown
        override val editorDirectionColor = gbLightAqua

        // Yellow-Green
        override val editorNumberColor = gbLightYellow

        // Green
        override val editorCommentColor = gbLightGreen

        // Orange-Brown
        override val editorStringColor = gbLightOrange

        // Red
        override val editorErrorColor = gbLightRed

        // Background highlight
        override val editorSelectedLineColor = gbBg0Soft
    }

    override val plotter = object : IThemePlotter {

        override val primaryBackgroundColor = ui.primaryBackground
        override val secondaryBackgroundColor = ui.secondaryBackground

        override val lineColor = ui.primaryTextColor

        override val gridColor = secondaryBackgroundColor.interpolate(lineColor, 0.15)
        override val gridTextColor = secondaryBackgroundColor.interpolate(lineColor, 0.4)

        override val redColor = gbLightRed
        override val blueColor = gbLightBlue

        override val highlightColor = gbLightYellow
        override val editColor = gbLightPurple

        override val robotMainColor = gbLightYellow
        override val robotDisplayColor = gbBg1
        override val robotWheelColor = lineColor
        override val robotSensorColor = robotWheelColor.interpolate(robotMainColor, 0.1)
        override val robotButtonColor = gbFg1
    }

    override val traverser = DefaultLightTheme.traverser
}
