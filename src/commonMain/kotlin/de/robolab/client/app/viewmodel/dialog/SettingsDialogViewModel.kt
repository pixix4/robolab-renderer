package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.controller.SystemController
import de.robolab.client.app.controller.dialog.SettingsDialogController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.theme.utils.ThemePropertySelectorMapper
import de.robolab.client.utils.PlatformDefaultPreferences
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.BuildInformation
import de.westermann.kobserve.not
import de.westermann.kobserve.or
import de.westermann.kobserve.property.mapBinding

class SettingsDialogViewModel(
    private val controller: SettingsDialogController
) : DialogViewModel("Settings") {

    val content = buildForm {
        labeledGroup("General") {
            labeledGroup("Appearance") {
                labeledEntry("Theme") {
                    select(
                        ThemePropertySelectorMapper.selectedThemeGroupProperty,
                        ThemePropertySelectorMapper.themeGroupList,
                    )
                    select(
                        ThemePropertySelectorMapper.selectedThemeVariantProperty,
                        ThemePropertySelectorMapper.themeVariantList,
                        enabledProperty = ThemePropertySelectorMapper.themeVariantEnabledProperty,
                    )
                }
                labeledEntry("Use system theme") {
                    input(PreferenceStorage.useSystemThemeProperty)
                }
                labeledEntry("Hide empty tab bar") {
                    input(PreferenceStorage.hideEmptyTabBarProperty)
                }
            }

            labeledGroup("Plotting") {
                labeledEntry("Export scale") {
                    input(
                        PreferenceStorage.exportScaleProperty,
                        0.1..100.0,
                        0.1,
                    )
                }
                labeledEntry("Animation time (in ms)") {
                    input(
                        PreferenceStorage.animationTimeProperty,
                        0.0..100000.0,
                        1.0,
                    )
                }
                labeledEntry("Render sender grouping chars") {
                    input(PreferenceStorage.renderSenderGroupingProperty)
                }
                labeledEntry("Auto scaling") {
                    input(PreferenceStorage.renderAutoScalingProperty)
                }
            }

            labeledGroup("Traverser") {
                labeledEntry("Auto expand") {
                    input(PreferenceStorage.traverserAutoExpandProperty)
                }
                labeledEntry("Delay calculation to improve ui responsiveness (in ms)") {
                    input(
                        PreferenceStorage.traverserDelayProperty,
                        0..100000,
                    )
                }
            }

            labeledGroup("Exam mode") {
                labeledEntry("Use remote state") {
                    input(PreferenceStorage.useRemoteExamStateProperty)
                }
                labeledEntry("Active") {
                    input(
                        PreferenceStorage.examActiveProperty,
                        enabledProperty = !PreferenceStorage.useRemoteExamStateProperty,
                    )
                }

                val enabledProperty =
                    !(PreferenceStorage.useRemoteExamStateProperty or !PreferenceStorage.examActiveProperty)

                labeledEntry("Exam planet ids") {
                    input(
                        PreferenceStorage.examPlanetsProperty,
                        enabledProperty = enabledProperty,
                    )
                }
            }
        }

        labeledGroup("Connection") {
            labeledGroup("Remote server") {
                if (SystemController.fixedRemoteUrl == null) {
                    labeledEntry("Server uri") {
                        input(
                            PreferenceStorage.remoteServerUrlProperty,
                            typeHint = FormContentViewModel.InputTypeHint.URL
                        )

                        if (!controller.isDesktop) {
                            button(
                                MaterialIcon.SETTINGS_BACKUP_RESTORE,
                                description = "Load default uri"
                            ) {
                                PreferenceStorage.remoteServerUrl = PlatformDefaultPreferences.remoteServerUri
                            }
                        }
                    }
                }

                labeledEntry("Server version") {
                    input(controller.serverVersionProperty)
                }
                labeledEntry("Server authentication") {
                    input(controller.serverAuthenticationProperty)
                    button("(Re)authenticate") {
                        controller.requestAuthToken()
                    }
                }

                if (controller.isDesktop) {
                    labeledGroup("Local planet directory") {
                        labeledEntry("Directory") {
                            input(PreferenceStorage.remoteFilesProperty)
                            button(MaterialIcon.FOLDER_OPEN) {
                                controller.openDirectory()
                            }
                        }
                    }
                }

                labeledGroup("MQTT") {
                    labeledEntry("") {
                        button("Load remote config") {
                            controller.loadMqttSettings()
                        }
                    }
                    labeledEntry("Server uri") {
                        input(PreferenceStorage.serverUriProperty)
                    }
                    labeledEntry("Username") {
                        input(PreferenceStorage.usernameProperty)
                    }
                    labeledEntry("Password") {
                        input(
                            PreferenceStorage.passwordProperty,
                            typeHint = FormContentViewModel.InputTypeHint.PASSWORD,
                        )
                    }
                    labeledEntry("Log uri") {
                        input(PreferenceStorage.logUriProperty)
                        input(
                            PreferenceStorage.logCountProperty,
                            0..100000,
                        )
                    }
                    labeledEntry("Client id") {
                        input(PreferenceStorage.clientIdProperty)
                    }
                }
            }
        }

        labeledGroup("Information") {
            for ((topic, content) in BuildInformation.dataMap) {
                labeledGroup(topic) {
                    for ((key, value) in content) {
                        labeledEntry(key) {
                            input(value.mapBinding { it.toString() })
                        }
                    }
                }
            }
        }

        labeledGroup("Advanced") {
            labeledGroup("Debugging") {
                labeledEntry("Log level") {
                    select(PreferenceStorage.logLevelProperty) {
                        it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                }

                labeledEntry("Debug render status") {
                    input(PreferenceStorage.debugStatusProperty)
                }
                labeledEntry("Debug render hierarchy") {
                    input(PreferenceStorage.debugHierarchyProperty)
                }

                labeledEntry("") {
                    button("Reset all settings") {
                        PreferenceStorage.clear()
                    }
                }
            }
        }
    }
}
