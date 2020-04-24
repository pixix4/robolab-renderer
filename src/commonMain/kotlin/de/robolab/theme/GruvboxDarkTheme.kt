package de.robolab.theme

import de.robolab.renderer.data.Color

object GruvboxDarkTheme : ITheme {

    // ----------------------------------------------------------------------------
    // Dark Mode
    // ----------------------------------------------------------------------------

    // Dark Background
    private val gbBg0 = Color(40, 40, 40)
    private val gbBg0Hard = Color(29, 32, 33)
    private val gbBg0Soft = Color(50, 48, 47)
    private val gbBg1 = Color(60, 56, 54)
    private val gbBg2 = Color(80, 73, 69)
    private val gbBg3 = Color(102, 92, 84)
    private val gbBg4 = Color(124, 111, 100)

    // Dark Foreground
    private val gbFg0 = Color(251, 241, 199)
    private val gbFg1 = Color(235, 219, 178)
    private val gbFg2 = Color(213, 196, 161)
    private val gbFg3 = Color(189, 174, 147)
    private val gbFg4 = Color(168, 153, 132)

    // Dark Colors
    private val gbDarkRed = Color(204, 36, 29)
    private val gbDarkGreen = Color(152, 151, 26)
    private val gbDarkYellow = Color(215, 153, 33)
    private val gbDarkBlue = Color(69, 133, 136)
    private val gbDarkPurple = Color(177, 98, 134)
    private val gbDarkAqua = Color(104, 157, 106)
    private val gbDarkOrange = Color(214, 93, 14)
    private val gbDarkGray = Color(146, 131, 116)

    private val gbLightRed = Color(251, 73, 52)
    private val gbLightGreen = Color(184, 187, 38)
    private val gbLightYellow = Color(250, 189, 47)
    private val gbLightBlue = Color(131, 165, 152)
    private val gbLightPurple = Color(211, 134, 155)
    private val gbLightAqua = Color(142, 192, 124)
    private val gbLightOrange = Color(243, 128, 25)
    private val gbLightGray = Color(168, 153, 132)

    override val ui = object : IThemeUi {

        override val primaryBackground = gbBg0Hard
        override val primaryHoverBackground = gbBg0

        override val secondaryBackground = gbBg0Soft
        override val secondaryHoverBackground = gbBg1

        override val tertiaryBackground = gbBg2
        override val tertiaryHoverBackground = gbBg3

        override val primaryTextColor = gbFg1
        override val secondaryTextColor = gbFg4

        override val themeColor = gbLightRed
        override val themeHoverColor = gbDarkRed
        override val themePrimaryText = gbBg0Hard
        override val themeSecondaryText = gbBg1

        override val borderColor = gbBg4

        override val successColor = gbDarkGreen
        override val successTextColor = gbFg1

        override val warnColor = gbDarkYellow
        override val warnTextColor = gbFg1

        override val errorColor = gbDarkRed
        override val errorTextColor = gbFg1
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

    override val traverser = LightTheme.traverser
}
