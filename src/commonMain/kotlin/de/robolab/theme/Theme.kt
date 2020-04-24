package de.robolab.theme

enum class Theme(val label: String, val theme: ITheme) {
    LIGHT("Light", LightTheme),
    DARK("Dark", DarkTheme),
    GRUVBOX_LIGHT("Gruvbox light", GruvboxLightTheme),
    GRUVBOX_DARK("Gruvbox dark", GruvboxDarkTheme);

    companion object {
        val DEFAULT = LIGHT
    }
}
