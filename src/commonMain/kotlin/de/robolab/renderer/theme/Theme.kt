package de.robolab.renderer.theme

enum class Theme(val label: String, val theme: ITheme) {
    LIGHT("Light", LightTheme),
    DARK("Dark", DarkTheme);

    companion object {
        val DEFAULT = LIGHT
    }
}
