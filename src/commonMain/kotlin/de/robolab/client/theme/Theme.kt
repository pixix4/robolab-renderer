package de.robolab.client.theme

enum class Theme(val group: String, val isDarkMode: Boolean?, val theme: ITheme) {
    LIGHT("Default", false, LightTheme),
    DARK("Default", true, DarkTheme),
    GRUVBOX_LIGHT("Gruvbox", false, GruvboxLightTheme),
    GRUVBOX_DARK("Gruvbox", true, GruvboxDarkTheme),
    NORD_LIGHT("Nord", false, NordLightTheme),
    NORD_DARK("Nord", true, NordDarkTheme);

    val label = group + when (isDarkMode) {
        true -> " dark"
        false -> " light"
        else -> ""
    }

    fun getThemeByMode(isDarkMode: Boolean?): Theme {
        if (this.isDarkMode == isDarkMode || this.isDarkMode == null || isDarkMode == null) {
            return this
        }

        val inverseTheme = values().find { it.group == group && it.isDarkMode == isDarkMode }
        if (inverseTheme != null) {
            return inverseTheme
        }

        // Break recursive call
        if (this == DEFAULT) {
            return this
        }

        return DEFAULT.getThemeByMode(isDarkMode)
    }

    companion object {
        val DEFAULT = LIGHT
    }
}
