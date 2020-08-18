package de.robolab.client.ui.views

import de.robolab.client.app.controller.ToolBarController
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.ui.dialog.Dialog
import de.robolab.client.ui.dialog.SettingsDialog
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import kotlinx.browser.window

class ToolBar(private val toolBarController: ToolBarController) : ViewCollection<View>() {

    val navigationBarActiveProperty = property(true)
    val infoBarActiveProperty = property(window.innerWidth >= 1500)

    private fun Button.bindIcon(iconProperty: ObservableValue<MaterialIcon?>) {
        var iconView: IconView? = null

        val icon = iconProperty.value
        if (icon != null) {
            iconView = iconView(icon)
        }

        iconProperty.onChange {
            iconView?.let { remove(it) }
            val newIcon = iconProperty.value
            if (newIcon != null) {
                iconView = iconView(newIcon)
            }
        }
    }

    private fun updateToolBarActions(toolBarActions: BoxView, actionList: List<List<ToolBarEntry>>) {
        toolBarActions.clear()

        for (group in actionList) {
            toolBarActions.buttonGroup {
                for (button in group) {
                    button(button.nameProperty) {
                        bindIcon(button.iconProperty)

                        classList.bind("active", button.selectedProperty)
                        disabledProperty.bind(!button.enabledProperty)

                        property(this::title).bind(button.toolTipProperty)

                        onClick {
                            button.onClick()
                        }
                    }
                }
            }
        }
    }

    private fun BoxView.setupToolbar(property: ObservableValue<List<List<ToolBarEntry>>>) {
        val toolBarAction = boxView("tool-bar-actions")
        updateToolBarActions(toolBarAction, property.value)
        property.onChange {
            updateToolBarActions(toolBarAction, property.value)
        }
    }

    init {
        classList.bind("navigation-bar-active", navigationBarActiveProperty)

        boxView("tool-bar-left") {
            button {
                iconView(MaterialIcon.MENU)
                title = "Toggle navigation bar"

                classList.bind("active", navigationBarActiveProperty)

                onClick {
                    navigationBarActiveProperty.value = !navigationBarActiveProperty.value
                }
            }

            setupToolbar(toolBarController.leftActionListProperty)
        }

        textView(toolBarController.titleProperty.mapBinding { it }) {
            classList += "tool-bar-center"
        }

        boxView("tool-bar-right") {

            setupToolbar(toolBarController.rightActionListProperty)

            buttonGroup {
                button {
                    iconView(MaterialIcon.REMOVE)
                    title = "Zoom out"

                    onClick {
                        toolBarController.zoomOut()
                    }
                }
                button(toolBarController.zoomProperty) {
                    title = "Reset zoom"

                    onClick {
                        toolBarController.resetZoom()
                    }
                }
                button {
                    iconView(MaterialIcon.ADD)
                    title = "Zoom in"

                    onClick {
                        toolBarController.zoomIn()
                    }
                }
            }

            buttonGroup {
                button {
                    iconView(MaterialIcon.VIEW_AGENDA)
                    title = "Window layout"

                    onClick {
                        ContextMenuView.open(Point(it.clientX, it.clientY), "Window layout") {
                            action("Split vertical") {
                                toolBarController.splitVertical()
                            }
                            action("Split horizontal") {
                                toolBarController.splitHorizontal()
                            }
                            action("Close window") {
                                toolBarController.closeWindow()
                            }
                            menu("Fixed layout") {
                                for (row in 1..3) {
                                    for (col in 1..3) {
                                        action("${row}x$col layout") {
                                            toolBarController.setGridLayout(row, col)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                button {
                    iconView(MaterialIcon.SETTINGS)
                    title = "Open settings"

                    onClick {
                        SettingsDialog.open()
                    }
                }
            }

            buttonGroup {
                button {
                    iconView(MaterialIcon.MENU)
                    title = "Toggle info bar"

                    classList.bind("active", infoBarActiveProperty)

                    onClick {
                        infoBarActiveProperty.value = !infoBarActiveProperty.value
                    }
                }
            }
        }
    }
}
