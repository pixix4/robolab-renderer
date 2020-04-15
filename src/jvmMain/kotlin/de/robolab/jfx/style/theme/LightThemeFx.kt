package de.robolab.jfx.style.theme

import javafx.scene.paint.Color

object LightThemeFx: IThemeFx {

    override val primaryBackground = Color.web("#FBFBFB")
    override val primaryHoverBackground = Color.web("#FFFFFF")

    override val secondaryBackground = Color.web("#EEEEEE")
    override val secondaryHoverBackground = Color.web("#F5F5F5")

    override val tertiaryBackground = Color.web("#E0E0E0")
    override val tertiaryHoverBackground = Color.web("#E9E9E9")

    override val primaryTextColor = Color.web("#333333")
    override val secondaryTextColor = Color.web("#888")

    override val themeColor = Color.web("#c0392b")
    override val themeText = Color.web("#FFFFFF")

    override val borderColor = Color.web("#D0D0D0")

    override val successColor = Color.web("#2ecc71")
    override val successTextColor = Color.web("#FFFFFF")

    override val warnColor = Color.web("#f1c40f")
    override val warnTextColor = Color.web("#FFFFFF")

    override val errorColor = Color.web("#e74c3c")
    override val errorTextColor = Color.web("#FFFFFF")

    override val editorKeywordColor = Color.web("#0000ff")
    override val editorDirectionColor = Color.web("#806732")
    override val editorNumberColor = Color.web("#098658")
    override val editorCommentColor = Color.web("#008000")
    override val editorStringColor = Color.web("#a31515")
    override val editorErrorColor = Color.web("#e74c3c")
    override val editorSelectedLineColor = Color.web("#d6eaff")
}
