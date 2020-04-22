package de.robolab.web.dialog

import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.Property
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.*

class SettingsDialog() : Dialog("Settings") {

    override fun BoxView.buildContent() {
        dialogFormGroup("Appearance") {
            dialogFormEntry("Theme") {
                selectView(PreferenceStorage.selectedThemeProperty) {
                    disabledProperty.bind(PreferenceStorage.useSystemThemeProperty)
                }
            }
            dialogFormEntry("Use system theme") {
                label(checkbox(PreferenceStorage.useSystemThemeProperty))
            }
        }

        dialogFormGroup("Plotting") {
            dialogFormEntry("Export scale") {
                inputView(InputType.NUMBER, PreferenceStorage.exportScaleProperty.bindStringParsing()) {
                    min = 0.1
                    max = 100.0
                    step = 0.1
                }
            }
        }

        dialogFormGroup("Connection") {
            dialogFormEntry("Server uri") {
                inputView(PreferenceStorage.serverUriProperty)
            }
            dialogFormEntry("Username") {
                inputView(PreferenceStorage.usernameProperty)
            }
            dialogFormEntry("Password") {
                inputView(InputType.PASSWORD, PreferenceStorage.passwordProperty)
            }
            dialogFormEntry("Client id") {
                inputView(PreferenceStorage.clientIdProperty)
            }
        }

        dialogFormGroup("Advanced") {
            dialogFormEntry("Log level") {
                selectView(PreferenceStorage.logLevelProperty)
            }

            button("Reset all settings") {
                onClick {
                    PreferenceStorage.reset()
                }
            }
        }
    }
}
