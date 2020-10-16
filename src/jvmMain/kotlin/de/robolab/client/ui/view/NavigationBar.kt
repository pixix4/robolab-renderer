package de.robolab.client.ui.view

import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.updater.Downloader
import de.robolab.client.utils.progressReader
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.flattenListBinding
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import javafx.application.Platform
import javafx.css.Styleable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class NavigationBar(
    private val navigationBarController: NavigationBarController,
    fileImportController: FileImportController,
    progressListener: Downloader.ProgressListener
) : View() {

    private val logger = Logger(this)

    private fun HBox.setupTabs() {
        clear()

        var last: HBox? = null
        spacer()
        for ((index, tab) in navigationBarController.tabListProperty.value.withIndex()) {
            last = hbox {
                addClass(MainStyle.tabBarTab)
                bindClass(MainStyle.active, navigationBarController.tabProperty.mapBinding { it == tab })
                setOnMouseClicked {
                    navigationBarController.tabIndexProperty.value = index
                }

                spacer()
                icon(tab.icon.toFx())
                spacer()
                tab.label.onChange.now {
                    tooltip(tab.label.value)
                }
                hgrow = Priority.ALWAYS
            }
        }
        last?.addPseudoClass("last")
        spacer()
    }

    override val root = vbox {
        addClass(MainStyle.navigationBar)

        hbox {
            addClass(MainStyle.tabBar)
            addClass(MainStyle.tabBarSide)
            hgrow = Priority.ALWAYS

            navigationBarController.tabListProperty.onChange.now {
                setupTabs()
            }
        }

        hbox {
            addClass(MainStyle.navigationBarBackButton)
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER_LEFT

            vbox {
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                icon(MaterialIcon.ARROW_BACK, "1.3em")
            }

            val labelText = navigationBarController.backButtonLabelProperty
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
                navigationBarController.onBackButtonClick()
            }

            style {
                padding = box(0.5.em)
            }

            val isVisible = navigationBarController.backButtonLabelProperty.mapBinding { it != null }.toFx()

            visibleWhen(isVisible)
            managedWhen(isVisible)
        }

        val entryList = navigationBarController.entryListProperty.flattenListBinding().toFx()

        listview(entryList) {
            vgrow = Priority.ALWAYS

            setCellFactory {
                SmartListCell()
            }

            cellFormat { provider ->
                graphic = vbox {
                    addClass(MainStyle.listCellGraphic)

                    hbox {
                        style {
                            padding = box(0.4.em, 0.5.em)
                        }

                        bindClass(MainStyle.disabled, !provider.enabledProperty)
                        vbox {
                            label(provider.nameProperty.toFx()) {
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
                        val menu = provider.contextMenu(Point.ZERO)

                        if (menu != null) {
                            contextMenu = menu.toFx()
                        }
                    }
                }
            }

            addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
                var selectedItem = this.selectedItem

                if (event.target.isInsideRow()) {
                    var target = event.target
                    while (target != null) {
                        if (target is SmartListCell<*>) {
                            val item = target.item as? INavigationBarEntry
                            if (item != selectedItem && item != null) {
                                selectedItem = item
                                selectionModel.select(item)
                            }
                        }
                        target = if (target is Node) target.parent else null
                    }
                }
                if (event.clickCount == 1 && (event.button == MouseButton.PRIMARY || event.button == MouseButton.MIDDLE) && selectedItem != null && event.target.isInsideRow()) {
                    val asNewTab = event.button == MouseButton.MIDDLE || event.isControlDown
                    selectedItem.open(asNewTab)
                }
            }

            addEventFilter(KeyEvent.KEY_PRESSED) { event ->
                val selectedItem = this.selectedItem
                if (event.code == KeyCode.ENTER && !event.isMetaDown && selectedItem != null) {
                    val asNewTab = event.isControlDown
                    selectedItem.open(asNewTab)
                }
            }

            entryList.onChange {
                Platform.runLater {
                    refresh()
                }
            }
        }

        hbox {
            hgrow = Priority.ALWAYS
            addClass(MainStyle.navigationBarSearchBox)

            buttonGroup(true) {
                hgrow = Priority.ALWAYS
                textfield(navigationBarController.searchStringProperty.toFx()) {
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.MAX_VALUE
                    promptText = "Search…"

                    setOnAction {
                        navigationBarController.submitSearch()
                    }
                }
                button {
                    graphic = iconNoAdd(MaterialIcon.PUBLISH)

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
                                    val progressReader = file.progressReader(progressListener)
                                    fileImportController.importFile(
                                        file.name,
                                        progressReader.lineSequence()
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
    }
}

fun Styleable.bindClass(clazz: CssRule, property: ObservableValue<Boolean>) {
    property.onChange {
        toggleClass(clazz, property.value)
    }
    toggleClass(clazz, property.value)
}
