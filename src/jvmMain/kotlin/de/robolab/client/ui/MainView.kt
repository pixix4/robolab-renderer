package de.robolab.client.ui

import de.robolab.client.app.controller.MainController
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.dialog.UpdateDialog
import de.robolab.client.ui.view.*
import de.robolab.client.utils.progressReader
import de.robolab.client.utils.runAfterTimeout
import de.robolab.common.utils.Logger
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*
import java.util.prefs.Preferences
import kotlin.system.exitProcess

class MainView : View() {

    private val logger = Logger("MainView")
    private val mainController = MainController(MainController.Args())

    override val root = borderpane {
        val window = this

        window.prefWidth = 1200.0
        window.prefHeight = 800.0

        titleProperty.bind(mainController.applicationTitleProperty.toFx())
        setStageIcon(Image("icon.png"))

        Platform.runLater {
            requestFocus()
        }

        val statusBar = StatusBar(mainController.statusBarController, mainController.uiController)
        val navigationBar =
            NavigationBar(mainController.navigationBarController, mainController.fileImportController, statusBar)
        left {
            stackpane {
                add(navigationBar)
                visibleWhen(mainController.uiController.navigationBarVisibleProperty.toFx())
                managedWhen(mainController.uiController.navigationBarVisibleProperty.toFx())
            }
        }
        mainController.uiController.navigationBarWidthProperty.onChange {
            val value = mainController.uiController.navigationBarWidthProperty.value
            navigationBar.root.minWidth = value
            navigationBar.root.prefWidth = value
            navigationBar.root.maxWidth = value
        }
        navigationBar.root.widthProperty().onChange {
            if (it > 1.0 && it != mainController.uiController.navigationBarWidthProperty.value) {
                mainController.uiController.setNavigationBarWidth(it, true)
            }
        }

        val infoBar = InfoBar(mainController.infoBarController)
        right {
            stackpane {
                add(infoBar)
                visibleWhen(mainController.uiController.infoBarVisibleProperty.toFx())
                managedWhen(mainController.uiController.infoBarVisibleProperty.toFx())
            }
        }
        mainController.uiController.infoBarWidthProperty.onChange {
            val value = mainController.uiController.infoBarWidthProperty.value
            infoBar.root.minWidth = value
            infoBar.root.prefWidth = value
            infoBar.root.maxWidth = value
        }
        infoBar.root.widthProperty().onChange {
            if (it > 1.0 && it != mainController.uiController.infoBarWidthProperty.value) {
                mainController.uiController.setInfoBarWidth(it, true)
            }
        }

        top {
            stackpane {
                add(ToolBar(mainController.toolBarController))
                visibleWhen(mainController.uiController.toolBarVisibleProperty.toFx())
                managedWhen(mainController.uiController.toolBarVisibleProperty.toFx())
            }
        }

        center {
            vbox {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                add(TabBar(mainController.tabController))
                add(MainCanvas(mainController.canvasController, mainController.uiController))
            }
        }

        bottom {
            stackpane {
                add(statusBar)
                visibleWhen(mainController.uiController.statusBarVisibleProperty.toFx())
                managedWhen(mainController.uiController.statusBarVisibleProperty.toFx())
            }
        }

        setOnDragOver { event ->
            if (event.gestureSource != this &&
                event.dragboard.hasFiles() &&
                event.dragboard.files.any {
                    mainController.fileImportController.isFileSupported(it.name)
                }
            ) {
                event.acceptTransferModes(TransferMode.COPY)
            }
            event.consume()
        }

        // Drop from native wayland applications can throw a NPE
        // This is a JavaFX bug and cannot be fixed
        setOnDragDropped { event ->
            var success = false
            if (event.dragboard.hasFiles()) {
                val fileList = event.dragboard.files.toList()
                GlobalScope.launch(Dispatchers.Default) {
                    for (file in fileList) {
                        try {
                            val progressReader = file.progressReader(statusBar)
                            mainController.fileImportController.importFile(
                                file.name,
                                progressReader.lineSequence()
                            )
                        } catch (e: Exception) {
                            logger.w { "Cannot import file '${file.absolutePath}'" }
                            logger.w { e }
                        }
                    }
                }
                success = true
            }
            event.isDropCompleted = success
            event.consume()
        }

        runAfterTimeout(1000) {
            UpdateDialog.openIfUpdateAvailable()
        }

        mainController.finishSetup()
    }

    private fun getPreferences(nodeName: String? = null): Preferences {
        return if (nodeName != null) Preferences.userRoot()
            .node(nodeName) else Preferences.userNodeForPackage(FX.getApplication(scope)!!.javaClass)
    }

    override fun onUndock() {
        val p = getPreferences()

        p.putDouble("w", primaryStage.width)
        p.putDouble("h", primaryStage.height)

        p.putBoolean("useX", true)
        p.putDouble("x", primaryStage.y)
        p.putBoolean("useY", true)
        p.putDouble("y", primaryStage.y)

        p.putBoolean("maximized", primaryStage.isMaximized)

        exitProcess(0)
    }

    override fun onDock() {
        val p = getPreferences()

        val w = p.getDouble("w", 1200.0)
        val h = p.getDouble("h", 800.0)

        val useX = p.getBoolean("useX", false)
        val x = p.getDouble("x", 0.0)
        val useY = p.getBoolean("useY", false)
        val y = p.getDouble("y", 0.0)

        val maximized = p.getBoolean("maximized", false)

        super.onDock()

        // primaryStage.isMaximized = true
        primaryStage.width = w
        primaryStage.height = h

        if (useX) {
            primaryStage.x = x
        }
        if (useY) {
            primaryStage.y = y
        }

        if (maximized) {
            primaryStage.isMaximized = true
        }

        primaryStage.requestFocus()
    }
}
