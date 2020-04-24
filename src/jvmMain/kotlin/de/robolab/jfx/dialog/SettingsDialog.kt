package de.robolab.jfx.dialog

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.utils.iconNoAdd
import de.robolab.theme.Theme
import de.robolab.utils.Logger
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.not
import javafx.scene.text.FontWeight
import javafx.util.StringConverter
import tornadofx.*

class SettingsDialog: View() {

    override val root = vbox {
        toolbar {
            style {
                padding = box(0.6.em, 1.5.em, 0.6.em, 1.5.em)
            }

            label("Settings") {
                style {
                    fontWeight = FontWeight.BOLD
                    fontSize = 1.2.em
                }
            }
            spacer()
            button {
                graphic = iconNoAdd(MaterialIcon.CLOSE)

                setOnAction {
                    close()
                }
            }
        }

        vbox {
            style {
                padding = box(1.em)
            }

            form {
                fieldset("Appearance") {
                    field("Theme") {
                        combobox(PreferenceStorage.selectedThemeProperty.toFx(), Theme.values().toList()) {
                            converter = object: StringConverter<Theme>() {
                                override fun toString(obj: Theme?): String {
                                    return obj?.label ?: "null"
                                }

                                override fun fromString(string: String?): Theme {
                                    if (string == null) return  Theme.DEFAULT
                                    return Theme.values().find { it.label.equals(string, true) } ?: Theme.DEFAULT
                                }

                            }
                            enableWhen {
                                PreferenceStorage.useSystemThemeProperty.not().toFx()
                            }
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
                            converter = object: StringConverter<Logger.Level>() {
                                override fun toString(obj: Logger.Level?): String {
                                    return obj?.name?.toLowerCase()?.capitalize() ?: "null"
                                }

                                override fun fromString(string: String?): Logger.Level {
                                    if (string == null) return  PreferenceStorage.logLevelProperty.default
                                    return Logger.Level.values().find { it.name.equals(string, true) } ?: PreferenceStorage.logLevelProperty.default
                                }
                            }}
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
    }
}

class DoubleStringConverter(private val default: Double) : StringConverter<Double>() {
    override fun toString(obj: Double?): String {
        return obj?.toString() ?: default.toString()
    }

    override fun fromString(string: String?): Double {
        return string?.toDoubleOrNull() ?: default
    }
}


class IntStringConverter(private val default: Int) : StringConverter<Int>() {
    override fun toString(obj: Int?): String {
        return obj?.toString() ?: default.toString()
    }

    override fun fromString(string: String?): Int {
        return string?.toIntOrNull() ?: default
    }
}
