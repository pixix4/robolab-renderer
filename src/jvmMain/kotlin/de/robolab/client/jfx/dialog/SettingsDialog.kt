package de.robolab.client.jfx.dialog

import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Logger
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
                    textfield(
                        PreferenceStorage.exportScaleProperty.toFx(),
                        DoubleStringConverter(PreferenceStorage.exportScaleProperty.default)
                    )
                }
                field("Animation time") {
                    textfield(
                        PreferenceStorage.animationTimeProperty.toFx(),
                        DoubleStringConverter(PreferenceStorage.animationTimeProperty.default)
                    )
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

                field("Log uri") {
                    textfield(PreferenceStorage.logUriProperty.toFx())
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

                field("Enable render debugging") {
                    checkbox("", PreferenceStorage.debugModeProperty.toFx())
                }
                field("Reset all settings") {
                    button("Reset") {
                        setOnAction {
                            PreferenceStorage.clear()
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
