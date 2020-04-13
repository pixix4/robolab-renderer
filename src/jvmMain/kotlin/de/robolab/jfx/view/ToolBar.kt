package de.robolab.jfx.view

import de.robolab.app.controller.ToolBarController
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.utils.buttonGroup
import de.westermann.kobserve.property.FunctionAccessor
import javafx.scene.layout.HBox
import javafx.scene.text.FontWeight
import tornadofx.*

class ToolBar(private val toolBarController: ToolBarController) : View() {

    private lateinit var toolBarActions: HBox

    private fun updateToolBarActions() {
        toolBarActions.clear()

        val actionList = toolBarController.actionListProperty.value
        for (group in actionList) {
            toolBarActions.buttonGroup {
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

                        enableWhen(button.enabledProperty.toFx())
                    }
                }

                paddingRight = 8
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
        label(toolBarController.titleProperty.toFx()) {
            style {
                fontWeight = FontWeight.BOLD
            }
        }
        spacer()
        buttonGroup {
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
