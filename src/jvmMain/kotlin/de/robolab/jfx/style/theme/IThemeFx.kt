package de.robolab.jfx.style.theme

import javafx.scene.paint.Color

interface IThemeFx {
    val primaryBackground: Color
    val primaryHoverBackground: Color

    val secondaryBackground: Color
    val secondaryHoverBackground: Color

    val tertiaryBackground: Color
    val tertiaryHoverBackground: Color

    val primaryTextColor: Color
    val secondaryTextColor: Color

    val themeColor: Color
    val themeText: Color

    val borderColor: Color

    val successColor: Color
    val successTextColor: Color

    val warnColor: Color
    val warnTextColor: Color

    val errorColor: Color
    val errorTextColor: Color

    val editorKeywordColor: Color
    val editorDirectionColor: Color
    val editorNumberColor: Color
    val editorCommentColor: Color
    val editorStringColor: Color
    val editorErrorColor: Color
    val editorSelectedLineColor: Color
}
