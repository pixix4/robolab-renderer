package de.robolab.theme

enum class Theme(val group: String, private val isDarkMode: Boolean, val theme: ITheme) {
    LIGHT("Default", false, LightTheme),
    DARK("Default", true, DarkTheme),
    GRUVBOX_LIGHT("Gruvbox", false, GruvboxLightTheme),
    GRUVBOX_DARK("Gruvbox", true, GruvboxDarkTheme);

    val label = "$group ${if (isDarkMode) "dark" else "light"}"

    fun getThemeByMode(isDarkMode: Boolean): Theme {
        if (this.isDarkMode == isDarkMode) {
            return this
        }

        val inverseTheme = Theme.values().find { it.group == group && it.isDarkMode == isDarkMode }
        if (inverseTheme != null) {
            return inverseTheme
        }

        if (this == DEFAULT) {
            return this
        }

        return DEFAULT.getThemeByMode(isDarkMode)
    }

    companion object {
        val DEFAULT = LIGHT
    }
}
