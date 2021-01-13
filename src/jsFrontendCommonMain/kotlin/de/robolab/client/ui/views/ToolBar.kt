package de.robolab.client.ui.views

import de.robolab.client.app.controller.ToolBarController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.ui.dialog.DownloadDialog
import de.robolab.client.ui.dialog.SettingsDialog
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.buildContextMenu
import de.robolab.client.utils.electron
import de.robolab.client.utils.noElectron
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*

class ToolBar(private val toolBarController: ToolBarController) : ViewCollection<View>() {

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

    private fun updateToolBarActions(toolBarActions: BoxView, actionList: List<List<ToolBarEntry>>?) {
        toolBarActions.clear()
        if (actionList == null) return

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

    private fun BoxView.setupToolbar(property: ObservableValue<List<List<ToolBarEntry>>?>) {
        val toolBarAction = boxView("tool-bar-actions")
        updateToolBarActions(toolBarAction, property.value)
        property.onChange {
            updateToolBarActions(toolBarAction, property.value)
        }
    }

    init {
        boxView("tool-bar-left") {
            buttonGroup {
                button {
                    iconView(MaterialIcon.MENU)
                    title = "Toggle navigation bar"

                    onClick {
                        toolBarController.uiController.navigationBarEnabledProperty.value =
                            !toolBarController.uiController.navigationBarEnabledProperty.value
                    }
                }
            }

            buttonGroup {
                button {
                    iconView(MaterialIcon.SETTINGS)
                    title = "Open settings"

                    onClick {
                        SettingsDialog.open(
                            toolBarController.fileNavigationManager.remoteServerVersionProperty,
                            toolBarController.fileNavigationManager.remoteServerAuthenticationProperty,
                            toolBarController::requestAuthToken,
                        ) {
                            toolBarController.loadMqttSettings {
                                it.wssURL
                            }
                        }
                    }
                }
                noElectron {
                    DownloadDialog.init {
                        button {
                            iconView(MaterialIcon.FILE_DOWNLOAD)
                            title = "Download RobolabRenderer client"

                            onClick {
                                DownloadDialog.open()
                            }
                        }
                    }
                }
            }

            buttonGroup {
                button {
                    iconView(MaterialIcon.FULLSCREEN)
                    title = "Fullscreen mode"

                    onClick {
                        toolBarController.toggleFullscreen()
                    }
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
                    iconView(MaterialIcon.UNDO)
                    title = "Undo last action"

                    disabledProperty.bind(!toolBarController.canUndoProperty)

                    onClick {
                        toolBarController.undo()
                    }
                }
                button {
                    iconView(MaterialIcon.REDO)
                    title = "Redo last Action"

                    disabledProperty.bind(!toolBarController.canRedoProperty)

                    onClick {
                        toolBarController.redo()
                    }
                }
            }

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
                    iconView(MaterialIcon.VIEW_QUILT) {
                        style {
                            fontSize = "1.35rem"
                        }
                    }
                    title = "Window layout"

                    onClick {
                        val menu = buildContextMenu(Point(it.clientX, it.clientY), "Window layout") {
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
                        if (menu != null) {
                            electron { electron ->
                                electron.menu(menu)
                            }
                            noElectron {
                                ContextMenuView.open(menu)
                            }
                        }

                    }
                }
            }

            buttonGroup {
                button {
                    iconView(MaterialIcon.MENU)
                    title = "Toggle info bar"

                    onClick {
                        toolBarController.uiController.infoBarEnabledProperty.value =
                            !toolBarController.uiController.infoBarEnabledProperty.value
                    }
                }
            }
        }
    }
}
