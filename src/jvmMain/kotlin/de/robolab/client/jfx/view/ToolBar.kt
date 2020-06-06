package de.robolab.client.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.client.app.controller.ToolBarController
import de.robolab.client.app.model.ToolBarEntry
import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.dialog.SettingsDialog
import de.robolab.client.jfx.style.MainStyle
import de.robolab.client.jfx.utils.buttonGroup
import de.robolab.client.jfx.utils.iconNoAdd
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.text.FontWeight
import tornadofx.*

class ToolBar(private val toolBarController: ToolBarController) : View() {

    val navigationBarActiveProperty = property(true)
    val infoBarActiveProperty = property(true)

    private fun ToolBarEntry.Icon.convert() = when (this) {
        ToolBarEntry.Icon.UNDO -> MaterialIcon.UNDO
        ToolBarEntry.Icon.REDO -> MaterialIcon.REDO
        ToolBarEntry.Icon.PREFERENCES -> MaterialIcon.BUILD
        ToolBarEntry.Icon.FLIP -> MaterialIcon.COMPARE
    }

    private fun Button.bindIcon(iconProperty: ObservableValue<ToolBarEntry.Icon?>) {
        graphicProperty().bind(iconProperty.mapBinding { it?.let { iconNoAdd(it.convert()) } }.toFx())
    }

    private fun updateToolBarActions(toolBarActions: HBox, actionList: List<List<ToolBarEntry>>) {
        toolBarActions.clear()

        for (group in actionList) {
            toolBarActions.buttonGroup {
                for (button in group) {
                    button(button.nameProperty.toFx()) {
                        bindIcon(button.iconProperty)
                        tooltipProperty().bind(button.toolTipProperty.mapBinding { Tooltip(it) }.toFx())

                        bindSelectedProperty(button.selectedProperty) {
                            button.onClick()
                        }

                        enableWhen(button.enabledProperty.toFx())
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }
                }

                paddingRight = 8
            }
        }
    }

    private fun HBox.setupToolbar(property: ObservableValue<List<List<ToolBarEntry>>>) {
        val toolBarAction = hbox { }
        updateToolBarActions(toolBarAction, property.value)
        property.onChange {
            updateToolBarActions(toolBarAction, property.value)
        }
    }


    override val root = scrollpane(fitToWidth = true) {
        addClass(MainStyle.toolBarContainer)

        vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        vmin = 0.0
        vmax = 0.0
        //hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        hbox {
            addClass(Stylesheet.toolBar, MainStyle.toolBar)
            hbox {
                hbox {
                    button {
                        graphic = iconNoAdd(MaterialIcon.MENU)
                        tooltip("Toggle navigation bar")

                        bindSelectedProperty(navigationBarActiveProperty)
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }

                    paddingRight = 8
                }

                setupToolbar(toolBarController.leftActionListProperty)
            }

            spacer()
            label(toolBarController.titleProperty.toFx()) {
                setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                style {
                    fontWeight = FontWeight.BOLD
                }
            }
            spacer()

            hbox {
                setupToolbar(toolBarController.rightActionListProperty)

                buttonGroup {
                    button {
                        graphic = iconNoAdd(MaterialIcon.REMOVE)
                        tooltip("Zoom out")
                        setOnAction {
                            toolBarController.zoomOut()
                        }
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }
                    button(toolBarController.zoomProperty.toFx()) {
                        tooltip("Reset zoom")
                        setOnAction {
                            toolBarController.resetZoom()
                        }
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }
                    button {
                        graphic = iconNoAdd(MaterialIcon.ADD)
                        tooltip("Zoom in")
                        setOnAction {
                            toolBarController.zoomIn()
                        }
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }

                    paddingRight = 8
                }

                buttonGroup {
                    button {
                        graphic = iconNoAdd(MaterialIcon.VIEW_AGENDA)
                        tooltip("Window layout")
                        contextmenu {
                            item("Split vertical") {
                                setOnAction {
                                    toolBarController.splitVertical()
                                }
                            }
                            item("Split horizontal") {
                                setOnAction {
                                    toolBarController.splitHorizontal()
                                }
                            }
                            item("Close window") {
                                setOnAction {
                                    toolBarController.closeWindow()
                                }
                            }
                            for (row in 1..3) {
                                for (col in 1..3) {
                                    item("${row}x$col layout") {
                                        setOnAction {
                                            toolBarController.setGridLayout(row, col)
                                        }
                                    }
                                }
                            }
                        }
                        setOnAction {
                            contextMenu.show(this, Side.BOTTOM, 0.0, 0.0)
                        }
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }
                    button {
                        graphic = iconNoAdd(MaterialIcon.SETTINGS)
                        tooltip("Open settings")

                        setOnAction {
                            SettingsDialog.open()
                        }
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }

                    paddingRight = 8
                }

                buttonGroup {
                    button {
                        graphic = iconNoAdd(MaterialIcon.MENU)
                        tooltip("Toggle info bar")

                        bindSelectedProperty(infoBarActiveProperty)
                        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
                    }
                }
            }
        }
    }
}

fun Button.bindSelectedProperty(property: ObservableValue<Boolean>, onClick: () -> Unit) {
    property.onChange {
        togglePseudoClass("selected", property.value)
    }
    togglePseudoClass("selected", property.value)

    setOnAction {
        onClick()
    }
}

fun Button.bindSelectedProperty(property: ObservableProperty<Boolean>) {
    property.onChange {
        togglePseudoClass("selected", property.value)
    }
    togglePseudoClass("selected", property.value)

    setOnAction {
        property.value = !property.value
    }
}
