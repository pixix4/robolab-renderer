package de.robolab.jfx.view

import de.robolab.app.controller.ToolBarController
import de.robolab.jfx.adapter.toFx
import de.westermann.kobserve.property.FunctionAccessor
import javafx.scene.layout.HBox
import tornadofx.*

class ToolBar(private val toolBarController: ToolBarController) : View() {

    private lateinit var toolBarActions: HBox

    private fun updateToolBarActions() {
        toolBarActions.clear()

        val actionList = toolBarController.actionListProperty.value
        for (group in actionList) {
            toolBarActions.hbox {
                for (button in group) {
                    val buttonProperty = de.westermann.kobserve.property.property(object : FunctionAccessor<Boolean> {
                        override fun set(value: Boolean): Boolean {
                            if (value) {
                                button.onClick()
                            }
                            return true
                        }

                        override fun get(): Boolean {
                            return button.activeProperty.value
                        }

                    }, button.activeProperty)

                    togglebutton(button.nameProperty.toFx()) {
                        selectedProperty().bindBidirectional(buttonProperty.toFx())
                        selectedProperty().onChange {
                            isSelected = buttonProperty.value
                        }
                    }
                }
            }
        }
    }

    override val root = toolbar {
        hbox {
            toolBarActions = hbox {

            }

            updateToolBarActions()
            toolBarController.actionListProperty.onChange {
                updateToolBarActions()
            }
        }
        spacer()
        label(toolBarController.titleProperty.toFx())
        spacer()
        hbox {
            button("-") {
                setOnAction {
                    toolBarController.zoomOut()
                }
            }
            button(toolBarController.zoomProperty.toFx()) {
                setOnAction {
                    toolBarController.resetZoom()
                }
            }
            button("+") {
                setOnAction {
                    toolBarController.zoomIn()
                }
            }
        }

    }
}
