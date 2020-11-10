package de.robolab.client.ui.dialog

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.ui.view.setIcon
import de.robolab.client.utils.MqttStorage
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.UpdateChannel
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.Logger
import de.westermann.kobserve.and
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.now
import de.westermann.kobserve.not
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import tornadofx.*


class SettingsDialog : GenericDialog() {

    private val requestAuthToken: () -> Unit by param()
    private val serverVersionProperty: ObservableValue<String> by param()
    private val serverAuthenticationProperty: ObservableValue<String> by param()

    private val serverVersionPropertyCopy = SimpleStringProperty("")
    private val serverAuthenticationPropertyCopy = SimpleStringProperty("")

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
                    field("Hide empty tab bar") {
                        checkbox("", PreferenceStorage.hideEmptyTabBarProperty.toFx())
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
                    field("Sender group names") {
                        checkbox("", PreferenceStorage.renderSenderGroupingProperty.toFx())
                    }
                    field("Auto scaling") {
                        checkbox("", PreferenceStorage.renderAutoScalingProperty.toFx())
                    }
                }

                fieldset("Exam mode") {
                    field("Use remote state") {
                        checkbox("", PreferenceStorage.useRemoteExamStateProperty.toFx())
                    }
                    field("Active") {
                        checkbox("", PreferenceStorage.examActiveProperty.toFx()) {
                            enableWhen((!PreferenceStorage.useRemoteExamStateProperty).toFx())
                        }
                    }

                    val enabled = (!PreferenceStorage.useRemoteExamStateProperty)
                        .and(PreferenceStorage.examActiveProperty)
                        .toFx()
                    field("Small Planet") {
                        textfield(PreferenceStorage.examSmallProperty.toFx()) {
                            enableWhen(enabled)
                        }
                    }
                    field("Large Planet") {
                        textfield(PreferenceStorage.examLargeProperty.toFx()) {
                            enableWhen(enabled)
                        }
                    }
                }

                if (UpdateDialog.isUpdateAllowed) {
                    fieldset("Update") {
                        field("Channel") {
                            combobox(
                                PreferenceStorage.autoUpdateChannelProperty.toFx(),
                                UpdateChannel.values().toList()
                            )
                        }
                        field(forceLabelIndent = true) {
                            button("Check for updates") {
                                setOnAction {
                                    UpdateDialog.open()
                                }
                            }
                        }
                    }
                }
            }
        }
        tab("Connection") {
            form {
                fieldset("Remote Server") {
                    field("Server uri") {
                        textfield(PreferenceStorage.remoteServerUrlProperty.toFx())
                    }
                    field("Server version") {
                        textfield(serverVersionPropertyCopy) {
                            isEditable = false
                        }
                    }
                    field("Server authentication") {
                        buttonGroup(true) {
                            textfield(serverAuthenticationPropertyCopy) {
                                isEditable = false
                            }
                            button("Authenticate") {
                                setOnAction {
                                    requestAuthToken()
                                }
                            }
                        }
                    }
                }
                fieldset("Local file sources") {
                    val fieldList = mutableListOf<Field>()

                    PreferenceStorage.remoteFilesProperty.onChange.now {
                        for (field in fieldList) {
                            field.removeFromParent()
                        }
                        fieldList.clear()

                        val textFields = mutableListOf<TextField>()

                        fun save() {
                            PreferenceStorage.remoteFiles =
                                textFields.map { it.text.trim() }.filter { it.isNotEmpty() }
                        }

                        for (connection in PreferenceStorage.remoteFiles) {
                            fieldList += field("Path") {
                                buttonGroup(true) {
                                    hgrow = Priority.ALWAYS
                                    val t = textfield(connection) {
                                        hgrow = Priority.ALWAYS

                                        setOnAction {
                                            save()
                                        }
                                    }
                                    textFields += t
                                    button {
                                        setIcon(MaterialIcon.FOLDER_OPEN)
                                        tooltip("Select folder")
                                        setOnAction {
                                            val dir = chooseDirectory("Choose directory")
                                            if (dir != null) {
                                                t.text = dir.absolutePath
                                                save()
                                            }
                                        }
                                    }
                                    button {
                                        setIcon(MaterialIcon.DELETE)
                                        tooltip("Delete source")
                                        setOnAction {
                                            t.text = ""
                                            save()
                                        }
                                    }
                                }
                            }
                        }
                        fieldList += field(forceLabelIndent = true) {
                            buttonGroup(true) {
                                hgrow = Priority.ALWAYS
                                val t = textfield("") {
                                    hgrow = Priority.ALWAYS

                                    setOnAction {
                                        save()
                                    }
                                }
                                textFields += t
                                button {
                                    setIcon(MaterialIcon.FOLDER_OPEN)
                                    tooltip("Select folder")
                                    setOnAction {
                                        val dir = chooseDirectory("Choose directory")
                                        if (dir != null) {
                                            t.text = dir.absolutePath
                                            save()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                    field("Message Storage") {
                        combobox(
                            PreferenceStorage.mqttStorageProperty.toFx(),
                            MqttStorage.values().toList()
                        )
                        tooltip("In Memory: Better performance\nDatabase: Less memory usage")
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

                    field("Debug render status") {
                        checkbox("", PreferenceStorage.debugStatusProperty.toFx())
                    }
                    field("Debug render hierarchy") {
                        checkbox("", PreferenceStorage.debugHierarchyProperty.toFx())
                    }
                    field("Reset all settings") {
                        button("Reset") {
                            setOnAction {
                                PreferenceStorage.clear()
                            }
                        }
                    }
                    field("Clear mqtt storage") {
                        button("Clear mqtt") {
                            setOnAction {
                                emit(MessageRepository.ClearStorageEvent)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()

        serverVersionPropertyCopy.value = serverVersionProperty.value
        serverVersionProperty.onChange {
            serverVersionPropertyCopy.value = serverVersionProperty.value
        }

        serverAuthenticationPropertyCopy.value = serverAuthenticationProperty.value
        serverAuthenticationProperty.onChange {
            serverAuthenticationPropertyCopy.value = serverAuthenticationProperty.value
        }
    }

    override val root = buildContent("Settings")

    companion object {
        fun open(
            serverVersionProperty: ObservableValue<String>,
            serverAuthenticationProperty: ObservableValue<String>,
            requestAuthToken: () -> Unit
        ) {
            open<SettingsDialog>(
                "requestAuthToken" to requestAuthToken,
                "serverVersionProperty" to serverVersionProperty,
                "serverAuthenticationProperty" to serverAuthenticationProperty,
            )
        }
    }
}
