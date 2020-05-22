package de.robolab.client.web

import de.robolab.client.app.controller.MainController
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.web.views.*
import de.robolab.common.utils.toDashCase
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

        dataset.bind("theme", PreferenceStorage.selectedThemeProperty.mapBinding { it.name.toDashCase() })

        val toolBar = ToolBar(mainController.toolBarController)
        +toolBar
        +SideBar(mainController.sideBarController, toolBar.sideBarActiveProperty)
        +StatusBar(mainController.statusBarController, toolBar.sideBarActiveProperty)
        +MainCanvas(mainController.canvasController, toolBar.sideBarActiveProperty, toolBar.infoBarActiveProperty)
        +InfoBar(mainController.infoBarController, toolBar.infoBarActiveProperty)
    }
}
