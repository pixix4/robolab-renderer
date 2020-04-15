package de.robolab.jfx.style.theme

import javafx.scene.paint.Color

object DarkThemeFx: IThemeFx {

    override val primaryBackground = Color.web("#0d0d0d")
    override val primaryHoverBackground = Color.web("#000000")

    override val secondaryBackground = Color.web("#303030")
    override val secondaryHoverBackground = Color.web("#242424")

    override val tertiaryBackground = Color.web("#4d4d4d")
    override val tertiaryHoverBackground = Color.web("#404040")

    override val primaryTextColor = Color.web("#ffffff")
    override val secondaryTextColor = Color.web("#9A9A9A")

    override val themeColor = Color.web("#e74c3c")
    override val themeText = Color.web("#FFFFFF")

    override val borderColor = Color.web("#555555")

    override val successColor = Color.web("#239a55")
    override val successTextColor = Color.web("#FFFFFF")

    override val warnColor = Color.web("#e59311")
    override val warnTextColor = Color.web("#FFFFFF")

    override val errorColor = Color.web("#b23528")
    override val errorTextColor = Color.web("#FFFFFF")

    override val editorKeywordColor = Color.web("#569cd6")
    override val editorDirectionColor = Color.web("#d0d0a1")
    override val editorNumberColor = Color.web("#b5cea8")
    override val editorCommentColor = Color.web("#648f50")
    override val editorStringColor = Color.web("#ce9178")
    override val editorErrorColor = Color.web("#b23528")
    override val editorSelectedLineColor = Color.web("#343a40")
}
