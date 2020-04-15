package de.robolab.jfx.view

import de.robolab.jfx.adapter.toFx
import de.robolab.renderer.theme.Theme
import de.robolab.utils.PreferenceStorage
import javafx.scene.Parent
import javafx.util.StringConverter
import tornadofx.*

class SettingsDialog:View() {

    override val root = vbox {
        style {
            padding = box(1.em)
        }

        form {
            fieldset("Appearance") {
                field("Theme") {
                    combobox(PreferenceStorage.selectedThemeProperty.toFx(), Theme.values().toList())
                }
            }

            fieldset("Plotting") {
                field("Export scale") {
                    textfield(PreferenceStorage.exportScaleProperty.toFx(), object: StringConverter<Double>() {
                        override fun toString(obj: Double?): String {
                            return obj?.toString() ?: PreferenceStorage.exportScaleProperty.default.toString()
                        }

                        override fun fromString(string: String?): Double {
                            return string?.toDoubleOrNull() ?: PreferenceStorage.exportScaleProperty.default
                        }
                    })
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
        }
    }
}
