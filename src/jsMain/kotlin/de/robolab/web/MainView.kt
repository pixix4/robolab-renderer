package de.robolab.web

import de.robolab.app.controller.MainController
import de.robolab.utils.PreferenceStorage
import de.robolab.web.views.*
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.components.init

fun main() {

    val mainController = MainController()

    watchSystemTheme()

    init {
        clear()

        dataset.bind("theme", PreferenceStorage.selectedThemeProperty.mapBinding { it.name.toLowerCase() })

        val toolBar = ToolBar(mainController.toolBarController)
        +toolBar
        +SideBar(mainController.sideBarController, toolBar.sideBarActiveProperty)
        +StatusBar(mainController.statusBarController)
        +MainCanvas(mainController.canvasController, toolBar.infoBarActiveProperty)
        +InfoBar(mainController.infoBarController, toolBar.infoBarActiveProperty)
    }
}
