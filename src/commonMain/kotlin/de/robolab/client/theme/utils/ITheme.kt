package de.robolab.client.theme.utils

import de.robolab.common.utils.Color

interface ITheme {

    val ui: IThemeUi
    val editor: IThemeEditor
    val plotter: IThemePlotter
    val traverser: IThemeTraverser
}

interface IThemeUi {

    val primaryBackground: Color
    val primaryHoverBackground: Color

    val secondaryBackground: Color
    val secondaryHoverBackground: Color

    val tertiaryBackground: Color
    val tertiaryHoverBackground: Color

    val primaryTextColor: Color
    val secondaryTextColor: Color

    val themeColor: Color
    val themeHoverColor: Color
    val themePrimaryText: Color
    val themeSecondaryText: Color

    val borderColor: Color

    val successColor: Color
    val successTextColor: Color

    val warnColor: Color
    val warnTextColor: Color

    val errorColor: Color
    val errorTextColor: Color
}

interface IThemeEditor {

    val editorKeywordColor: Color
    val editorDirectionColor: Color
    val editorNumberColor: Color
    val editorCommentColor: Color
    val editorStringColor: Color
    val editorErrorColor: Color
    val editorSelectedLineColor: Color
}

interface IThemePlotter {

    val primaryBackgroundColor: Color
    val secondaryBackgroundColor: Color

    val lineColor: Color

    val gridColor: Color
    val gridTextColor: Color

    val redColor: Color
    val blueColor: Color

    val highlightColor: Color
    val editColor: Color

    val robotMainColor: Color
    val robotDisplayColor: Color
    val robotWheelColor: Color
    val robotSensorColor: Color
    val robotButtonColor: Color
}

interface IThemeTraverser {

    val traverserCharacteristicCorrectColor: Color
    val traverserCharacteristicErrorColor: Color
    val traverserCharacteristicNorthColor: Color
    val traverserCharacteristicEastColor: Color
    val traverserCharacteristicSouthColor: Color
    val traverserCharacteristicWestColor: Color
}
