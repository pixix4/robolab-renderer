package de.robolab.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.app.controller.ToolBarController
import de.robolab.app.model.ToolBarEntry
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.dialog.SettingsDialog
import de.robolab.jfx.utils.buttonGroup
import de.robolab.jfx.utils.iconNoAdd
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.mapBinding
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.text.FontWeight
import tornadofx.*

class ToolBar(private val toolBarController: ToolBarController) : View() {

    val sideBarActiveProperty = de.westermann.kobserve.property.property(true)
    val infoBarActiveProperty = de.westermann.kobserve.property.property(true)

    private fun ToolBarEntry.Icon.convert() = when (this) {
        ToolBarEntry.Icon.UNDO -> MaterialIcon.UNDO
        ToolBarEntry.Icon.REDO -> MaterialIcon.REDO
        ToolBarEntry.Icon.PREFERENCES -> MaterialIcon.BUILD
        ToolBarEntry.Icon.FLIP -> MaterialIcon.COMPARE
    }

    private fun Button.bindIcon(iconProperty: ReadOnlyProperty<ToolBarEntry.Icon?>) {
        graphicProperty().bind(iconProperty.mapBinding { it?.let { iconNoAdd(it.convert()) } }.toFx())
    }

    private fun updateToolBarActions(toolBarActions: HBox, actionList: List<List<ToolBarEntry>>) {
        toolBarActions.clear()

        for (group in actionList) {
            toolBarActions.buttonGroup {
                for (button in group) {
                    button(button.nameProperty.toFx()) {
                        bindIcon(button.iconProperty)

                        bindSelectedProperty(button.selectedProperty) {
                            button.onClick()
                        }

                        enableWhen(button.enabledProperty.toFx())
                    }
                }

                paddingRight = 8
            }
        }
    }

    private fun HBox.setupToolbar(property: ReadOnlyProperty<List<List<ToolBarEntry>>>) {
        val toolBarAction = hbox { }
        updateToolBarActions(toolBarAction, property.value)
        property.onChange {
            updateToolBarActions(toolBarAction, property.value)
        }
    }


    override val root = toolbar {
        hbox {
            hbox {
                button {
                    graphic = iconNoAdd(MaterialIcon.MENU)

                    bindSelectedProperty(sideBarActiveProperty)
                }

                paddingRight = 8
            }

            setupToolbar(toolBarController.leftActionListProperty)
        }

        spacer()
        label(toolBarController.titleProperty.toFx()) {
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
                    setOnAction {
                        toolBarController.zoomOut()
                    }
                }
                button(toolBarController.zoomProperty.toFx()) {
                    setOnAction {
                        toolBarController.resetZoom()
                    }
                }
                button {
                    graphic = iconNoAdd(MaterialIcon.ADD)
                    setOnAction {
                        toolBarController.zoomIn()
                    }
                }

                paddingRight = 8
            }

            hbox {
                button {
                    graphic = iconNoAdd(MaterialIcon.SETTINGS)

                    setOnAction {
                        SettingsDialog.open()
                    }
                }

                paddingRight = 8
            }

            button {
                graphic = iconNoAdd(MaterialIcon.MENU)

                bindSelectedProperty(infoBarActiveProperty)
            }
        }
    }
}

fun Button.bindSelectedProperty(property: ReadOnlyProperty<Boolean>, onClick: () -> Unit) {
    property.onChange {
        togglePseudoClass("selected", property.value)
    }
    togglePseudoClass("selected", property.value)

    setOnAction {
        onClick()
    }
}

fun Button.bindSelectedProperty(property: Property<Boolean>) {
    property.onChange {
        togglePseudoClass("selected", property.value)
    }
    togglePseudoClass("selected", property.value)

    setOnAction {
        property.value = !property.value
    }
}
