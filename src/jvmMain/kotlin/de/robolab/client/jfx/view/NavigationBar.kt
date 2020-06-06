package de.robolab.client.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.INavigationBarPlottable
import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.style.MainStyle
import de.robolab.client.jfx.utils.buttonGroup
import de.robolab.client.jfx.utils.icon
import de.robolab.client.jfx.utils.iconNoAdd
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import javafx.css.Styleable
import javafx.scene.control.OverrunStyle
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class NavigationBar(
    navigationBarController: NavigationBarController,
    fileImportController: FileImportController
) : View() {

    private val logger = Logger(this)

    override val root = vbox {
        addClass(MainStyle.navigationBar)

        hbox {
            addClass(MainStyle.toolBar)
            hgrow = Priority.ALWAYS

            //spacer()
            buttonGroup {
                hgrow = Priority.ALWAYS
                for (tab in NavigationBarController.Tab.values()) {
                    button(tab.label) {
                        bindSelectedProperty(navigationBarController.tabProperty.mapBinding { it == tab }) {
                            navigationBarController.tabProperty.value = tab
                        }
                        textOverrun = OverrunStyle.CLIP

                        hgrow = Priority.ALWAYS
                    }
                }
            }
            //spacer()
        }

        hbox {
            buttonGroup {
                textfield(navigationBarController.searchStringProperty.toFx()) {
                    hgrow = Priority.ALWAYS
                    promptText = "Search…"
                }
                button {
                    graphic = iconNoAdd(MaterialIcon.ADD)

                    setOnAction {
                        val files = chooseFile(
                            "Import file",
                            fileImportController.supportedFiles.map { (label, types) ->
                                FileChooser.ExtensionFilter(label, *types.toTypedArray())
                            }.toTypedArray(),
                            owner = primaryStage
                        )

                        GlobalScope.launch(Dispatchers.Default) {
                            for (file in files) {
                                try {
                                    fileImportController.importFile(
                                        file.name,
                                        file.readText()
                                    )
                                } catch (e: Exception) {
                                    logger.w { "Cannot import file '${file.absolutePath}'" }
                                    logger.w { e }
                                }
                            }
                        }
                    }
                }
            }

            style {
                padding = box(0.5.em)
            }
        }

        hbox {
            hgrow = Priority.ALWAYS
            val navigationBarBackButton = hbox {
                addClass(MainStyle.navigationBarBackButton)
                hgrow = Priority.ALWAYS

                label(navigationBarController.selectedGroupProperty.nullableFlatMapBinding { it?.tabNameProperty }
                    .mapBinding {
                        it ?: ""
                    }.toFx()) {
                    style {
                        fontWeight = FontWeight.BOLD
                        padding = box(0.5.em, 1.em)
                    }
                }


                setOnMouseClicked {
                    navigationBarController.closeGroup()
                }
            }

            val isVisible = navigationBarController.selectedGroupProperty.mapBinding { it != null }

            if (!isVisible.value) {
                navigationBarBackButton.removeFromParent()
            }

            isVisible.onChange {
                if (isVisible.value) {
                    add(navigationBarBackButton)
                } else {
                    navigationBarBackButton.removeFromParent()
                }
            }
        }

        listview(navigationBarController.filteredEntryListProperty.mapBinding { it.toFx() }.toFx()) {
            vgrow = Priority.ALWAYS

            setCellFactory {
                SmartListCell()
            }

            cellFormat { provider ->
                val selectedProperty = navigationBarController.selectedElementListProperty.mapBinding { provider in it }

                graphic = vbox {
                    addClass(MainStyle.listCellGraphic)
                    bindClass(MainStyle.active, selectedProperty)

                    hbox {
                        style {
                            padding = box(0.4.em, 0.5.em)
                        }
                        if (provider is INavigationBarPlottable) {
                            bindClass(MainStyle.disabled, !provider.enabledProperty)
                        }
                        vbox {
                            label(provider.titleProperty.toFx()) {
                                style {
                                    fontWeight = FontWeight.BOLD
                                }
                            }
                            label(provider.subtitleProperty.toFx()) {
                                style {
                                    fontSize = 0.8.em
                                }
                            }
                        }
                        spacer()
                        vbox {
                            spacer()
                            icon(MaterialIcon.SAVE) {
                                tooltip("Unsaved changes")
                                visibleWhen(provider.unsavedChangesProperty.toFx())
                            }
                            spacer()
                        }
                    }

                    setOnContextMenuRequested {
                        if (provider.hasContextMenu) {
                            val menu = provider.buildContextMenu(Point.ZERO)

                            contextMenu = menu.toFx()
                        }
                    }
                }
            }

            onUserSelect(1) {
                navigationBarController.open(it)
            }
        }

        hbox {
            bindClass(
                MainStyle.success,
                navigationBarController.statusColor.mapBinding { it == NavigationBarController.StatusColor.SUCCESS })
            bindClass(
                MainStyle.warn,
                navigationBarController.statusColor.mapBinding { it == NavigationBarController.StatusColor.WARN })
            bindClass(
                MainStyle.error,
                navigationBarController.statusColor.mapBinding { it == NavigationBarController.StatusColor.ERROR })

            style {
                prefHeight = 2.em
                padding = box(0.4.em, 0.5.em)
            }

            label(navigationBarController.statusMessage.toFx())
            spacer()
            label(navigationBarController.statusActionLabel.toFx()) {
                setOnMouseClicked {
                    navigationBarController.onStatusAction()
                }

                style {
                    underline = true
                }
            }
        }
    }
}

fun Styleable.bindClass(clazz: CssRule, property: ObservableValue<Boolean>) {
    property.onChange {
        toggleClass(clazz, property.value)
    }
    toggleClass(clazz, property.value)
}
