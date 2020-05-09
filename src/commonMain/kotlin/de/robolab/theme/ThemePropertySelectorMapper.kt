package de.robolab.theme

import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.and
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.property.DelegatePropertyAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

object ThemePropertySelectorMapper {

    val selectedThemeGroupProperty: ObservableProperty<String> = property(object : DelegatePropertyAccessor<String> {

        override fun set(value: String) {
            val groupThemes = Theme.values().filter { it.group == value }

            if (groupThemes.size == 1) {
                PreferenceStorage.selectedTheme = groupThemes.first()
                return
            }

            val isDarkMode = PreferenceStorage.selectedTheme.isDarkMode == true

            PreferenceStorage.selectedTheme = groupThemes.firstOrNull { it.isDarkMode == isDarkMode }
                    ?: Theme.DEFAULT
        }

        override fun get(): String {
            return PreferenceStorage.selectedTheme.group
        }
    }, PreferenceStorage.selectedThemeProperty)
    val themeGroupList: List<String> = Theme.values().map { it.group }.distinct()


    val selectedThemeVariantProperty: ObservableProperty<String> = property(object : DelegatePropertyAccessor<String> {

        override fun set(value: String) {
            val isDarkMode = value == "Dark"
            if (!PreferenceStorage.useSystemTheme) {
                PreferenceStorage.selectedTheme = PreferenceStorage.selectedTheme.getThemeByMode(isDarkMode)
            }
        }

        override fun get(): String {
            return if (PreferenceStorage.selectedTheme.isDarkMode == true) "Dark" else "Light"
        }
    }, PreferenceStorage.selectedThemeProperty)
    val themeVariantList: List<String> = listOf("Light", "Dark")


    val themeVariantEnabledProperty: ObservableValue<Boolean> = !PreferenceStorage.useSystemThemeProperty and PreferenceStorage.selectedThemeProperty.mapBinding { it.isDarkMode != null }
}