package de.robolab.web.views

import de.robolab.app.controller.ToolBarController
import de.robolab.app.model.ToolBarEntry
import de.robolab.web.dialog.Dialog
import de.robolab.web.dialog.SettingsDialog
import de.robolab.web.views.utils.buttonGroup
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import kotlin.browser.window

class ToolBar(private val toolBarController: ToolBarController) : ViewCollection<View>() {

    val sideBarActiveProperty = property(true)
    val infoBarActiveProperty = property(window.innerWidth >= 1500)

    private fun ToolBarEntry.Icon.convert() = when (this) {
        ToolBarEntry.Icon.UNDO -> MaterialIcon.UNDO
        ToolBarEntry.Icon.REDO -> MaterialIcon.REDO
        ToolBarEntry.Icon.PREFERENCES -> MaterialIcon.BUILD
    }

    private fun Button.bindIcon(iconProperty: ReadOnlyProperty<ToolBarEntry.Icon?>) {
        var iconView: IconView? = null

        val icon = iconProperty.value
        if (icon != null) {
            iconView = iconView(icon.convert())
        }

        iconProperty.onChange {
            iconView?.let { remove(it) }
            val newIcon = iconProperty.value
            if (newIcon != null) {
                iconView = iconView(newIcon.convert())
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

    private fun BoxView.setupToolbar(property: ReadOnlyProperty<List<List<ToolBarEntry>>>) {
        val toolBarAction = boxView("tool-bar-actions")
        updateToolBarActions(toolBarAction,property.value)
        property.onChange {
            updateToolBarActions(toolBarAction, property.value)
        }
    }

    init {
        classList.bind("side-bar-active", sideBarActiveProperty)

        boxView("tool-bar-left") {
            button {
                iconView(MaterialIcon.MENU)
                title = "Toggle side bar"

                classList.bind("active", sideBarActiveProperty)

                onClick {
                    sideBarActiveProperty.value = !sideBarActiveProperty.value
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

            button {
                iconView(MaterialIcon.SETTINGS)
                title = "Open settings"

                onClick {
                    Dialog.open(SettingsDialog())
                }
            }

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
