package de.robolab.web.views

import de.robolab.app.controller.ToolBarController
import de.robolab.web.views.utils.buttonGroup
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class ToolBar(private val toolBarController: ToolBarController) : ViewCollection<View>() {

    val sideBarActiveProperty = property(true)

    private lateinit var toolBarActions: BoxView

    private fun updateToolBarActions() {
        toolBarActions.clear()

        val actionList = toolBarController.actionListProperty.value
        for (group in actionList) {
            toolBarActions.buttonGroup {
                for (button in group) {
                    button(button.nameProperty) {
                        classList.bind("active", button.activeProperty)
                        onClick {
                            button.onClick()
                        }
                    }
                }
            }
        }
    }

    init {
        boxView("tool-bar-left") {
            button {
                classList += "menu"
                iconView(MaterialIcon.MENU)

                onClick {
                    sideBarActiveProperty.value = !sideBarActiveProperty.value
                }
            }

            toolBarActions = boxView("tool-bar-actions")
            updateToolBarActions()
            toolBarController.actionListProperty.onChange {
                updateToolBarActions()
            }
        }

        textView(toolBarController.titleProperty.mapBinding { it }) {
            classList += "tool-bar-center"
        }

        boxView("tool-bar-right") {
            buttonGroup {
                button("-") {
                    onClick {
                        toolBarController.zoomOut()
                    }
                }
                button(toolBarController.zoomProperty) {
                    onClick {
                        toolBarController.resetZoom()
                    }
                }
                button("+") {
                    onClick {
                        toolBarController.zoomIn()
                    }
                }
            }
        }
    }
}
