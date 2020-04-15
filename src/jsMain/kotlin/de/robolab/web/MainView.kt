package de.robolab.web

import de.robolab.app.controller.MainController
import de.robolab.utils.PreferenceStorage
import de.robolab.web.views.*
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.init
import kotlin.browser.document

fun main() {

    val mainController = MainController()

    watchSystemTheme()
    property(document::title).bind(mainController.applicationTitleProperty)

    init {
        clear()

        dataset.bind("theme", PreferenceStorage.selectedThemeProperty.mapBinding { it.name.toLowerCase() })

        val toolBar = ToolBar(mainController.toolBarController)
        +toolBar
        +SideBar(mainController.sideBarController, toolBar.sideBarActiveProperty)
        +StatusBar(mainController.statusBarController, toolBar.sideBarActiveProperty)
        +MainCanvas(mainController.canvasController, toolBar.sideBarActiveProperty, toolBar.infoBarActiveProperty)
        +InfoBar(mainController.infoBarController, toolBar.infoBarActiveProperty)
    }
}
