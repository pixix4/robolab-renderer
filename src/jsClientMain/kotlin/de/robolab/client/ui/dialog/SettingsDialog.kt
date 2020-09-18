package de.robolab.client.ui.dialog

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FileNavigationRoot
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.BuildInformation
import de.westermann.kobserve.and
import de.westermann.kobserve.event.now
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.components.*

class SettingsDialog private constructor(): Dialog("Settings") {

    init {
        tab("General") {
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
                dialogFormEntry("Animation time") {
                    inputView(InputType.NUMBER, PreferenceStorage.animationTimeProperty.bindStringParsing()) {
                        min = 0.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("Render sender grouping chars") {
                    label(checkbox(PreferenceStorage.renderSenderGroupingProperty))
                }
            }

            dialogFormGroup("Exam mode") {
                dialogFormEntry("Use remote state") {
                    label(checkbox(PreferenceStorage.useRemoteExamStateProperty))
                }
                dialogFormEntry("Active") {
                    label(checkbox(PreferenceStorage.examActiveProperty) {
                        disabledProperty.bind(!PreferenceStorage.useRemoteExamStateProperty)
                    })
                }

                val enabled = !(!PreferenceStorage.useRemoteExamStateProperty)
                    .and(PreferenceStorage.examActiveProperty)

                dialogFormEntry("Small Planet") {
                    inputView(PreferenceStorage.examSmallProperty) {
                        disabledProperty.bind(!enabled)
                    }
                }
                dialogFormEntry("Large Planet") {
                    inputView(PreferenceStorage.examLargeProperty) {
                        disabledProperty.bind(!PreferenceStorage.examActiveProperty)
                    }
                }
            }
        }

        tab("Connection") {
            dialogFormGroup("MQTT") {
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
                dialogFormEntry("Log uri") {
                    inputView(PreferenceStorage.logUriProperty)
                }
            }

            dialogFormGroup("Files") {
                title = FileNavigationRoot.loaderFactoryList.joinToString("\n") { it.usage }
                boxView {
                    PreferenceStorage.fileServerProperty.onChange.now {
                        clear()
                        val textFields = mutableListOf<InputView>()

                        fun save() {
                            PreferenceStorage.fileServer =
                                textFields.map { it.value.trim() }.filter { it.isNotEmpty() }
                        }

                        for (connection in PreferenceStorage.fileServer) {
                            dialogFormEntry("") {
                                classList += "button-group"
                                classList += "button-form-group"
                                val t = inputView(connection)
                                textFields += t
                                button {
                                    iconView(MaterialIcon.DONE)
                                    onClick {
                                        save()
                                    }
                                }
                                button {
                                    iconView(MaterialIcon.DELETE)
                                    onClick {
                                        t.value = ""
                                        save()
                                    }
                                }
                            }
                        }
                        dialogFormEntry("") {
                            classList += "button-group"
                            classList += "button-form-group"
                            textFields += inputView("")
                            button {
                                iconView(MaterialIcon.ADD)
                                onClick {
                                    save()
                                }
                            }
                        }
                    }
                }
            }
        }

        tab("Information") {
            for ((topic, content) in BuildInformation.dataMap) {
                dialogFormGroup(topic) {
                    for ((key, value) in content) {
                        dialogFormEntry(key) {
                            inputView(value.mapBinding { it.toString() }) {
                                readonly = true
                            }
                        }
                    }
                }
            }
        }

        tab("Advanced") {
            dialogFormGroup("Debugging") {
                dialogFormEntry("Log level") {
                    selectView(PreferenceStorage.logLevelProperty, transform = { it.name.toLowerCase().capitalize() })
                }

                dialogFormEntry("Enable render debugging") {
                    label(checkbox(PreferenceStorage.debugModeProperty))
                }

                button("Reset all settings") {
                    onClick {
                        PreferenceStorage.clear()
                    }
                }
            }
        }
    }

    companion object {
        fun open() {
            open(SettingsDialog())
        }
    }
}