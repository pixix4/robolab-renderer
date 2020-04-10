package de.robolab.web

import de.robolab.app.controller.MainController
import de.robolab.web.views.MainCanvas
import de.robolab.web.views.SideBar
import de.robolab.web.views.StatusBar
import de.robolab.web.views.ToolBar
import de.westermann.kwebview.components.init

fun main() {

    val mainController = MainController()

    init {
        clear()

        val toolBar = ToolBar(mainController.toolBarController)
        +toolBar
        +SideBar(mainController.sideBarController, toolBar.sideBarActiveProperty)
        +StatusBar(mainController.statusBarController)
        +MainCanvas(mainController.canvasController)
    }
}
