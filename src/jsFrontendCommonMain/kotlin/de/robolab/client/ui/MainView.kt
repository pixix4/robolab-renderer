package de.robolab.client.ui

import de.robolab.client.app.controller.MainController
import de.robolab.client.ui.views.*
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.EnvironmentStorage
import de.robolab.common.utils.toDashCase
import de.westermann.kobserve.not
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.bindStyleProperty
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.init
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun initMainView(args: MainController.Args) {
    val mainController = MainController(args)

    watchSystemTheme()
    property(document::title).bind(mainController.applicationTitleProperty)

    init {
        clear()

        val uiController = mainController.uiController
        dataset.bind("theme", PreferenceStorage.selectedThemeProperty.mapBinding { it.name.toDashCase() })

        bindStyleProperty(
            "--navigation-bar-width",
            uiController.navigationBarWidthProperty.join(uiController.navigationBarVisibleProperty) { width, visible ->
                if (visible) "${width}px" else "0px"
            })
        bindStyleProperty(
            "--info-bar-width",
            uiController.infoBarWidthProperty.join(uiController.infoBarVisibleProperty) { width, visible ->
                if (visible) "${width}px" else "0px"
            })

        boxView("app") {
            classList.bind("navigation-bar-active", uiController.navigationBarVisibleProperty)
            classList.bind("info-bar-active", uiController.infoBarVisibleProperty)
            classList.bind("fullscreen", uiController.fullscreenProperty)
            classList.bind("tab-bar-hidden", !mainController.tabController.visibleProperty)

            +ToolBar(mainController.toolBarController)
            +TabBar(mainController.tabController)
            +NavigationBar(
                mainController.navigationBarController,
                mainController.fileImportController,
                mainController.uiController
            )
            +StatusBar(mainController.statusBarController)

            +MainCanvas(
                mainController.canvasController,
                mainController.uiController
            )
            +InfoBar(mainController.infoBarController, mainController.uiController)
        }
        onDragOver { event ->
            event.stopPropagation()
            event.preventDefault()

            event.dataTransfer?.dropEffect = "copy"
        }

        onDrop { event ->
            event.stopPropagation()
            event.preventDefault()
            val files = event.dataTransfer?.files?.let { fileList ->
                (0 until fileList.length).mapNotNull { fileList.item(it) }
            } ?: emptyList()

            GlobalScope.launch(Dispatchers.Default) {
                for (file in files) {
                    val content = file.readText()
                    if (content != null) {
                        mainController.fileImportController.importFile(file.name, file.lineSequence())
                    }
                }
            }
        }

        mainController.finishSetup()
    }
}
