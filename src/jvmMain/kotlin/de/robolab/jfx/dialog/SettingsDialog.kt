package de.robolab.jfx.dialog

import de.robolab.jfx.adapter.toFx
import de.robolab.theme.ThemePropertySelectorMapper
import de.robolab.utils.Logger
import de.robolab.utils.PreferenceStorage
import javafx.util.StringConverter
import tornadofx.*


class SettingsDialog : GenericDialog() {

    override val root = buildContent("Settings") {
        form {
            fieldset("Appearance") {
                field("Theme") {
                    combobox(
                            ThemePropertySelectorMapper.selectedThemeGroupProperty.toFx(),
                            ThemePropertySelectorMapper.themeGroupList
                    )
                    combobox(
                            ThemePropertySelectorMapper.selectedThemeVariantProperty.toFx(),
                            ThemePropertySelectorMapper.themeVariantList
                    ) {
                        enableWhen(ThemePropertySelectorMapper.themeVariantEnabledProperty.toFx())
                    }
                }
                field("Use system theme") {
                    checkbox("", PreferenceStorage.useSystemThemeProperty.toFx())
                }
            }

            fieldset("Plotting") {
                field("Export scale") {
                    textfield(PreferenceStorage.exportScaleProperty.toFx(), DoubleStringConverter(PreferenceStorage.exportScaleProperty.default))
                }
            }

            fieldset("Connection") {
                field("Server uri") {
                    textfield(PreferenceStorage.serverUriProperty.toFx())
                }
                field("Username") {
                    textfield(PreferenceStorage.usernameProperty.toFx())
                }
                field("Password") {
                    passwordfield(PreferenceStorage.passwordProperty.toFx())
                }
                field("Client id") {
                    textfield(PreferenceStorage.clientIdProperty.toFx())
                }
            }

            fieldset("Advanced") {
                field("Log level") {
                    combobox(PreferenceStorage.logLevelProperty.toFx(), Logger.Level.values().toList()) {
                        converter = object : StringConverter<Logger.Level>() {
                            override fun toString(obj: Logger.Level?): String {
                                return obj?.name?.toLowerCase()?.capitalize() ?: "null"
                            }

                            override fun fromString(string: String?): Logger.Level {
                                if (string == null) return PreferenceStorage.logLevelProperty.default
                                return Logger.Level.values().find { it.name.equals(string, true) }
                                        ?: PreferenceStorage.logLevelProperty.default
                            }
                        }
                    }
                }
                field("Reset all settings") {
                    button("Reset") {
                        setOnAction {
                            PreferenceStorage.reset()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open() {
            open<SettingsDialog>()
        }
    }
}
