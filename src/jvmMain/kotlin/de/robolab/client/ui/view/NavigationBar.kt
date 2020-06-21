package de.robolab.client.ui.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.base.INavigationBarPlottable
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.ui.utils.icon
import de.robolab.client.ui.utils.iconNoAdd
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.now
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import javafx.css.Styleable
import javafx.geometry.Pos
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
                alignment = Pos.CENTER_LEFT

                vbox {
                    icon(MaterialIcon.ARROW_BACK) {
                        style {
                            fontSize = 1.2.em
                        }
                    }
                    paddingTop = 8
                }

                val labelText = navigationBarController.selectedGroupProperty
                    .nullableFlatMapBinding { it?.tabNameProperty }
                    .mapBinding { it ?: "" }

                label(labelText.toFx()) {
                    style {
                        fontWeight = FontWeight.BOLD
                        padding = box(0.2.em, 0.5.em)
                    }
                }
                tooltip(labelText.value)
                labelText.onChange {
                    tooltip(labelText.value)
                }

                setOnMouseClicked {
                    navigationBarController.closeGroup()
                }

                style {
                    padding = box(0.5.em)
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
                            vbox {
                                provider.statusIconProperty.onChange.now {
                                    clear()

                                    for (element in provider.statusIconProperty.value) {
                                        icon(element)
                                    }
                                }
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
