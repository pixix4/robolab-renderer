package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.ui.ToolBarController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.utils.contextMenu
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.toggle

class ToolBarViewModel(
    private val controller: ToolBarController
) : ViewModel {

    val titleProperty = controller.titleProperty

    private val leftList = buildFormContent {
        group {
            button(
                MaterialIcon.MENU,
                description = "Toggle navigation bar"
            ) {
                controller.navigationBarEnabledProperty.toggle()
            }
        }
        group {
            button(
                MaterialIcon.SETTINGS,
                description = "Open settings"
            ) {
                controller.openSettingsDialog()
            }
        }
        group {
            button(
                controller.fullscreenProperty.mapBinding { if (it) MaterialIcon.FULLSCREEN_EXIT else MaterialIcon.FULLSCREEN },
                description = "Toggle fullscreen mode"
            ) {
                controller.fullscreenProperty.toggle()
            }
        }
    }
    private val rightList = buildFormContent { 
        group {
            button(
                MaterialIcon.UNDO,
                enabledProperty = controller.canUndoProperty,
                description = "Undo last action"
            ) {
                controller.undo()
            }
            button(
                MaterialIcon.REDO,
                enabledProperty = controller.canRedoProperty,
                description = "Redo last action"
            ) {
                controller.redo()
            }
        }
        group {
            button(MaterialIcon.REMOVE, description = "Zoom out") {
                controller.zoomOut()
            }
            button(controller.zoomProperty, description = "Reset zoom") {
                controller.resetZoom()
            }
            button(MaterialIcon.ADD, description = "Zoom in") {
                controller.zoomIn()
            }
        }
        group {
            button(MaterialIcon.HORIZONTAL_SPLIT, description = "Split horizontal") {
                controller.splitHorizontal()
            }
            button(MaterialIcon.VERTICAL_SPLIT, description = "Split vertical") {
                controller.splitVertical()
            }
            button(MaterialIcon.DASHBOARD, description = "Window layout") {
                it.contextMenu("Window layout") {
                    for (row in 1..3) {
                        for (col in 1..3) {
                            action("${row}x$col layout") {
                                controller.setGridLayout(row, col)
                            }
                        }
                    }
                }
            }
        }
        group {
            button(MaterialIcon.MENU) {
                controller.infoBarEnabledProperty.toggle()
            }
        }
    }

    val leftActionList = observableListOf<FormContentViewModel>()
    val rightActionList = observableListOf<FormContentViewModel>()

    init {
        controller.leftActionListProperty.onChange.now {
            leftActionList.sync(leftList.contentProperty + controller.leftActionListProperty.value)
        }
        controller.rightActionListProperty.onChange.now {
            rightActionList.sync(controller.rightActionListProperty.value + rightList.contentProperty)
        }
    }
}
