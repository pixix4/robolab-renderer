package de.robolab.client.web

import de.robolab.client.app.controller.MainController
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.web.views.*
import de.robolab.common.utils.Logger
import de.robolab.common.utils.toDashCase
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.init
import org.w3c.files.File
import org.w3c.files.FileReader
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

        onDragOver { event ->
            event.stopPropagation()
            event.preventDefault()

            event.dataTransfer?.dropEffect = "copy"
        }

        onDrop { event ->
            event.stopPropagation()
            event.preventDefault()
            val files = event.dataTransfer?.files?.let {fileList ->
                (0 until fileList.length).map { fileList.item(it)!! }
            } ?: return@onDrop

            for (file in files ) {
                importFile(file, mainController)
            }
        }
    }
}

private fun importFile(file: File, mainController: MainController) {
    val reader = FileReader()

    reader.onload = {
        val data = reader.result as? String

        if (data == null) {
            Logger("MainApp").w { "Cannot import dragged log file!" }
        } else {
            mainController.importLogFile(data)
        }
    }

    reader.onerror = {
        Logger("MainApp").w { "Cannot import dragged log file!" }
    }

    reader.readAsText(file)
}