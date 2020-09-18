package de.robolab.client.ui

import de.robolab.client.app.controller.MainController
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.utils.PreferenceStorage
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.image.Image
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
                    }

                    fieldset("Planet repo") {
                        field("Repo") {
                            textfield(planetFolder.toFx())
                            button("Select") {
                                setOnAction {
                                    val f = chooseDirectory("Select planet repo")

                                    if (f != null) {
                                        planetFolder.value = f.absolutePath
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
            PreferenceStorage.fileServer = if (planetFolder.value.isEmpty()) {
                emptyList()
            } else {
                listOf("directory://" + planetFolder.value)
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