package de.robolab.client.theme.utils

import de.robolab.client.theme.DefaultLightTheme
import de.robolab.common.utils.Color
import kotlin.math.min

fun Color.intensity(value: Double): Color {
    val (h, s, v) = toHsv()

    return Color.hsv(h, s, min(1.0, v * value))
}

object TermColorTheme : ITheme {

    private val termColors: ITermColors = ParsedITermColors()
    private val referenceBackgroundColor = if (termColors.backgroundColor.luminance() < 0.5)
        Color.BLACK else Color.WHITE
    private val referenceForegroundColor = if (referenceBackgroundColor == Color.WHITE)
        Color.BLACK else Color.WHITE

    override val ui = object : IThemeUi {

        override val primaryHoverBackground = termColors.backgroundColor.intensity(2.0).interpolate(
            referenceBackgroundColor, 0.2)
        override val primaryBackground = termColors.backgroundColor.intensity(2.0).interpolate(referenceBackgroundColor, 0.1)

        override val secondaryHoverBackground = termColors.backgroundColor
        override val secondaryBackground = termColors.backgroundColor.interpolate(termColors.foregroundColor, 0.05)

        override val tertiaryHoverBackground = termColors.backgroundColor.interpolate(termColors.foregroundColor, 0.1)
        override val tertiaryBackground = termColors.backgroundColor.interpolate(termColors.foregroundColor, 0.15)

        override val primaryTextColor = termColors.foregroundColor
        override val secondaryTextColor = termColors.foregroundColor.interpolate(termColors.backgroundColor, 0.4)

        override val themeColor = termColors.redColor
        override val themeHoverColor = termColors.redBrightColor
        override val themePrimaryText = termColors.foregroundColor
        override val themeSecondaryText = termColors.foregroundColor

        override val borderColor = termColors.backgroundColor.interpolate(termColors.foregroundColor, 0.4)

        override val successColor = termColors.greenColor
        override val successTextColor = termColors.foregroundColor

        override val warnColor = termColors.yellowColor
        override val warnTextColor = termColors.foregroundColor

        override val errorColor = termColors.redColor
        override val errorTextColor = termColors.foregroundColor
    }

    override val editor = object : IThemeEditor {

        // Blue
        override val editorKeywordColor = termColors.blueColor

        // Yellow-Brown
        override val editorDirectionColor = termColors.cyanColor

        // Yellow-Green
        override val editorNumberColor = termColors.yellowColor

        // Green
        override val editorCommentColor = termColors.greenColor

        // Orange-Brown
        override val editorStringColor = termColors.redBrightColor

        // Red
        override val editorErrorColor = termColors.redColor

        // Background highlight
        override val editorSelectedLineColor = ui.primaryHoverBackground
    }

    override val plotter = object : IThemePlotter {

        override val primaryBackgroundColor = ui.primaryBackground
        override val secondaryBackgroundColor = ui.secondaryBackground

        override val lineColor = ui.primaryTextColor

        override val gridColor = secondaryBackgroundColor.interpolate(lineColor, 0.15)
        override val gridTextColor = secondaryBackgroundColor.interpolate(lineColor, 0.4)

        override val redColor = termColors.redColor
        override val blueColor = termColors.blueColor

        override val highlightColor = termColors.yellowColor
        override val editColor = termColors.cyanBrightColor

        override val robotMainColor = termColors.yellowColor
        override val robotDisplayColor = ui.primaryBackground
        override val robotWheelColor = lineColor
        override val robotSensorColor = robotWheelColor.interpolate(robotMainColor, 0.1)
        override val robotButtonColor = ui.primaryTextColor
    }

    override val traverser = DefaultLightTheme.traverser
}
