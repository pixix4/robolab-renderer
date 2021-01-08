package de.robolab.client.ui.dialog

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.electron
import de.robolab.common.utils.BuildInformation
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.or
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.components.*
import kotlinx.browser.window

class SettingsDialog private constructor(
    private val requestAuthToken: () -> Unit,
    private val loadMqttSettings: () -> Unit,
    private val serverVersionProperty: ObservableValue<String>,
    private val serverAuthenticationProperty: ObservableValue<String>,
) : Dialog("Settings") {


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
                dialogFormEntry("Hide empty tab bar") {
                    label(checkbox(PreferenceStorage.hideEmptyTabBarProperty))
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
                dialogFormEntry("Auto scaling") {
                    label(checkbox(PreferenceStorage.renderAutoScalingProperty))
                }
            }

            dialogFormGroup("Exam mode") {
                dialogFormEntry("Use remote state") {
                    label(checkbox(PreferenceStorage.useRemoteExamStateProperty))
                }
                dialogFormEntry("Active") {
                    label(checkbox(PreferenceStorage.examActiveProperty) {
                        disabledProperty.bind(PreferenceStorage.useRemoteExamStateProperty)
                    })
                }

                val disabled = PreferenceStorage.useRemoteExamStateProperty or !PreferenceStorage.examActiveProperty

                dialogFormEntry("Small Planet") {
                    inputView(PreferenceStorage.examSmallProperty) {
                        disabledProperty.bind(disabled)
                    }
                }
                dialogFormEntry("Large Planet") {
                    inputView(PreferenceStorage.examLargeProperty) {
                        disabledProperty.bind(disabled)
                    }
                }
            }
        }

        tab("Connection") {
            dialogFormGroup("Remote server") {
                dialogFormEntry("Server uri", true) {

                    inputView(PreferenceStorage.remoteServerUrlProperty) {
                        type = InputType.URL
                    }
                    button {
                        iconView(MaterialIcon.SETTINGS_BACKUP_RESTORE)
                        title = "Restore default uri"
                        onClick {
                            PreferenceStorage.remoteServerUrl = window.location.href
                        }
                    }
                }

                dialogFormEntry("Server version") {
                    inputView(serverVersionProperty) {
                        readonly = true
                    }
                }
                dialogFormEntry("Server authentication", true) {
                    inputView(serverAuthenticationProperty) {
                        readonly = true
                    }
                    button("Authenticate") {
                        onClick {
                            requestAuthToken()
                        }
                    }
                }
            }

            electron { electron ->
                dialogFormGroup("Local planet directory") {
                    dialogFormEntry("Directory", true) {
                        inputView(PreferenceStorage.remoteFilesProperty)
                        button {
                            iconView(MaterialIcon.FOLDER_OPEN)

                            onClick {
                                electron.ipcRenderer.send("select-directory")
                                electron.ipcRenderer.once("select-directory") { _, args ->
                                    val path = args[0].unsafeCast<String>()
                                    PreferenceStorage.remoteFiles = path
                                }
                            }
                        }
                    }
                }
            }

            dialogFormGroup("MQTT") {
                dialogFormEntry("") {
                    button("Load remote config") {
                        onClick {
                            loadMqttSettings()
                        }
                    }
                }
                dialogFormEntry("Server uri") {
                    inputView(PreferenceStorage.serverUriProperty)
                }
                dialogFormEntry("Username") {
                    inputView(PreferenceStorage.usernameProperty)
                }
                dialogFormEntry("Password") {
                    inputView(InputType.PASSWORD, PreferenceStorage.passwordProperty)
                }
                dialogFormEntry("Log uri") {
                    classList += "button-group"
                    classList += "button-form-group"
                    inputView(PreferenceStorage.logUriProperty)
                    inputView(InputType.NUMBER, PreferenceStorage.logCountProperty.bindStringParsing()) {
                        min = 0.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("Client id") {
                    inputView(PreferenceStorage.clientIdProperty)
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

                dialogFormEntry("Debug render status") {
                    label(checkbox(PreferenceStorage.debugStatusProperty))
                }
                dialogFormEntry("Debug render hierarchy") {
                    label(checkbox(PreferenceStorage.debugHierarchyProperty))
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
        fun open(
            serverVersionProperty: ObservableValue<String>,
            serverAuthenticationProperty: ObservableValue<String>,
            requestAuthToken: () -> Unit,
            loadMqttSettings: () -> Unit,
        ) {
            open(
                SettingsDialog(
                    requestAuthToken,
                    loadMqttSettings,
                    serverVersionProperty,
                    serverAuthenticationProperty
                )
            )
        }
    }
}
