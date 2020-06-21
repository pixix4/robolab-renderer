package de.robolab.client.ui.dialog

import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.now
import javafx.scene.control.TextField
import javafx.util.StringConverter
import tornadofx.*


class SettingsDialog : GenericDialog() {

    init {
        tab("General") {
            form {
                fieldset("Appearance") {
                    field("Theme") {
                        combobox(
                            ThemePropertySelectorMapper.selectedThemeGroupProperty.toFx(),
                            ThemePropertySelectorMapper.themeGroupList
                        )
                    }
                    field("Variant") {
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
            }
        }
        tab("Connection") {
            form {
                fieldset("MQTT") {
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
                fieldset("Files") {
                    tooltip(MultiFilePlanetProvider.loaderFactoryList.joinToString("\n") { it.usage })
                    vbox {
                        PreferenceStorage.fileServerProperty.onChange.now {
                            clear()
                            val textFields = mutableListOf<TextField>()
                            for (connection in PreferenceStorage.fileServer) {
                                field(forceLabelIndent = true) {
                                    textFields += textfield(connection)
                                }
                            }
                            field(forceLabelIndent = true) {
                                textFields += textfield("")
                            }
                            field(forceLabelIndent = true) {
                                button("Apply file server connections") {
                                    setOnAction {
                                        PreferenceStorage.fileServer =
                                            textFields.map { it.text.trim() }.filter { it.isNotEmpty() }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tab("Information") {
            form {
                for ((topic, content) in BuildInformation.dataMap) {
                    fieldset(topic) {
                        for ((key, value) in content) {
                            field(key) {
                                label(value.toFx())
                            }
                        }
                    }
                }
            }
        }
        tab("Advanced") {
            form {
                fieldset {
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
    }

    override val root = buildContent("Settings")

    companion object {
        fun open() {
            open<SettingsDialog>()
        }
    }
}
