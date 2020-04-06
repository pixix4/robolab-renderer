package de.robolab.app.controller

import de.robolab.app.model.IPlottable
import de.westermann.kobserve.property.property

class MainController {

    val selectedEntryProperty = property<IPlottable?>(null)

    val sideBarController = SideBarController(selectedEntryProperty)
    val canvasController = CanvasController(selectedEntryProperty)
    val toolBarController = ToolBarController(selectedEntryProperty, canvasController)
    val statusBarController = StatusBarController(canvasController)
}