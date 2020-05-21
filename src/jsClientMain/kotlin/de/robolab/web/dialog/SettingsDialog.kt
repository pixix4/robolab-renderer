package de.robolab.web.dialog

import de.robolab.theme.ThemePropertySelectorMapper
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.not
import de.westermann.kwebview.components.*

class SettingsDialog() : Dialog("Settings") {

    override fun BoxView.buildContent() {
        dialogFormGroup("Appearance") {
            dialogFormEntry("Theme") {
                selectView<String>(
                        ThemePropertySelectorMapper.themeGroupList,
                        ThemePropertySelectorMapper.selectedThemeGroupProperty
                )
                selectView<String>(
                        ThemePropertySelectorMapper.themeVariantList,
                        ThemePropertySelectorMapper.selectedThemeVariantProperty
                ) {
                    disabledProperty.bind(!ThemePropertySelectorMapper.themeVariantEnabledProperty)
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
                selectView(PreferenceStorage.logLevelProperty, transform = { it.name.toLowerCase().capitalize() })
            }

            dialogFormEntry("Enable render debugging") {
                label(checkbox(PreferenceStorage.debugModeProperty))
            }

            button("Reset all settings") {
                onClick {
                    PreferenceStorage.reset()
                }
            }
        }
    }
}
