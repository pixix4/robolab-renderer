package de.robolab.client.ui

import de.robolab.client.app.controller.MainController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.ui.view.setIcon
import de.robolab.client.utils.PreferenceStorage
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import tornadofx.*
import kotlin.system.exitProcess

class SetupView : View() {

    private var openMainView = false

    private val planetFolder = de.westermann.kobserve.property.property("")

    override val root = vbox {
        title = "${MainController.APPLICATION_NAME} - Setup"
        setStageIcon(Image("icon.png"))

        Platform.runLater {
            requestFocus()
        }

        alignment = Pos.CENTER

        hbox {
            alignment = Pos.CENTER

            vbox {
                prefWidth = 570.0

                style {
                    padding = box(32.px, 64.px)
                }

                label(MainController.APPLICATION_NAME) {
                    style {
                        fontSize = 24.pt
                        fontWeight = FontWeight.BOLD
                        padding = box(8.px)
                    }
                }

                form {
                    fieldset("MQTT Connection") {
                        field("Server uri") {
                            textfield(PreferenceStorage.serverUriProperty.toFx())
                        }
                        field("Username") {
                            textfield(PreferenceStorage.usernameProperty.toFx())
                        }
                        field("Password") {
                            passwordfield(PreferenceStorage.passwordProperty.toFx())
                        }
                    }

                    fieldset("Remote planet repo") {
                        field("Server uri") {
                            textfield(PreferenceStorage.remoteServerUrlProperty.toFx())
                        }
                    }

                    fieldset("Local planet repo") {
                        field("Repo") {
                            buttonGroup(true) {
                                hgrow = Priority.ALWAYS
                                textfield(planetFolder.toFx()) {
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                }
                                button {
                                    setIcon(MaterialIcon.FOLDER_OPEN)
                                    tooltip("Select folder")

                                    setOnAction {
                                        val f = chooseDirectory("Select planet repo")

                                        if (f != null) {
                                            planetFolder.value = f.absolutePath
                                        }
                                    }
                                }
                            }
                        }
                    }

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

                    hbox {
                        spacer()
                        button("Start") {
                            setOnAction {
                                openMainView = true
                                close()
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        planetFolder.onChange {
            PreferenceStorage.remoteFiles = if (planetFolder.value.isEmpty()) {
                emptyList()
            } else {
                listOf(planetFolder.value)
            }
        }
    }

    override fun onUndock() {
        if (openMainView) {
            PreferenceStorage.firstStart = false
            val stage = find<MainView>()
                .openWindow(StageStyle.DECORATED, owner = null) ?: exitProcess(1)
            stage.toFront()
        } else {
            exitProcess(0)
        }
    }
}