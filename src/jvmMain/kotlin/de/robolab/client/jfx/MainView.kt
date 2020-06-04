package de.robolab.client.jfx

import de.robolab.client.app.controller.MainController
import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.view.*
import de.robolab.common.utils.Logger
import de.westermann.kobserve.property.DelegatePropertyAccessor
import de.westermann.kobserve.property.property
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
import java.lang.Exception
import java.util.prefs.Preferences
import kotlin.system.exitProcess


class MainView : View() {

    private val logger = Logger("MainView")
    private val mainController = MainController()

    override val root = hbox {
        val window = this

        window.prefWidth = 1200.0
        window.prefHeight = 800.0

        titleProperty.bind(mainController.applicationTitleProperty.toFx())
        setStageIcon(Image("icon.png"))

        Platform.runLater {
            requestFocus()
        }

        val toolBar = ToolBar(mainController.toolBarController)

        val sideBarContainer = stackpane {
            minWidth = 260.0

            add(NavigationBar(mainController.sideBarController))

            visibleWhen(toolBar.sideBarActiveProperty.toFx())
        }

        var lastSideBarWidth = 260.0
        val sideBarWidthProperty = property(object : DelegatePropertyAccessor<Double> {
            override fun set(value: Double) {
                sideBarContainer.minWidth = value
                sideBarContainer.prefWidth = value
                sideBarContainer.maxWidth = value
                if (value > 0.0) {
                    lastSideBarWidth = value
                }
            }

            override fun get(): Double {
                return sideBarContainer.width
            }
        })
        sideBarContainer.widthProperty().onChange {
            sideBarWidthProperty.onChange.emit(Unit)
        }
        toolBar.sideBarActiveProperty.onChange {
            if (toolBar.sideBarActiveProperty.value) {
                sideBarWidthProperty.value = lastSideBarWidth
            } else {
                sideBarWidthProperty.value = 0.0
            }
        }


        borderpane {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            val prefWidthBinding = window.widthProperty().subtract(sideBarContainer.widthProperty())
            prefWidthProperty().bind(prefWidthBinding)
            maxWidthProperty().bind(prefWidthBinding)
            minWidthProperty().bind(prefWidthBinding)

            val infoBar = InfoBar(mainController.infoBarController)
            right {
                stackpane {
                    add(infoBar)
                    visibleWhen { toolBar.infoBarActiveProperty.toFx() }
                }
            }


            var lastInfoBarWidth = 260.0
            val infoBarWidthProperty = property(object : DelegatePropertyAccessor<Double> {
                override fun set(value: Double) {
                    infoBar.root.minWidth = value
                    infoBar.root.prefWidth = value
                    infoBar.root.maxWidth = value
                    if (value > 0.0) {
                        lastInfoBarWidth = value
                    }
                }

                override fun get(): Double {
                    return infoBar.root.width
                }
            })
            infoBar.root.widthProperty().onChange {
                infoBarWidthProperty.onChange.emit(Unit)
            }
            toolBar.infoBarActiveProperty.onChange {
                if (toolBar.infoBarActiveProperty.value) {
                    infoBarWidthProperty.value = lastInfoBarWidth
                } else {
                    infoBarWidthProperty.value = 0.0
                }
            }

            top {
                add(toolBar)
            }
            center {
                val mainCanvas = MainCanvas(
                    mainController.canvasController,
                    toolBar.sideBarActiveProperty,
                    sideBarWidthProperty,
                    toolBar.infoBarActiveProperty,
                    infoBarWidthProperty
                )

                val centerWidthBinding = prefWidthBinding.subtract(
                    infoBar.root.widthProperty().doubleBinding(toolBar.infoBarActiveProperty.toFx()) {
                        (if (toolBar.infoBarActiveProperty.value) it?.toDouble() else null) ?: 0.0
                    })
                mainCanvas.root.prefWidthProperty().bind(centerWidthBinding)
                mainCanvas.root.maxWidthProperty().bind(centerWidthBinding)
                mainCanvas.root.minWidthProperty().bind(centerWidthBinding)

                add(mainCanvas)
            }
            bottom {
                add(StatusBar(mainController.statusBarController))
            }

        }

        setOnDragOver {  event ->
            if (event.gestureSource != this && event.dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        }

        // Drop from native wayland applications can throw a NPE
        // This is a JavaFX bug and cannot be fixed
        setOnDragDropped { event ->
            var success = false
            if (event.dragboard.hasFiles()) {
                for (file in event.dragboard.files) {
                    importFile(file)
                }
                success = true
            }
            event.isDropCompleted = success
            event.consume()
        }
    }

    private fun getPreferences(nodeName: String? = null): Preferences {
        return if (nodeName != null) Preferences.userRoot().node(nodeName) else Preferences.userNodeForPackage(FX.getApplication(scope)!!.javaClass)
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
            primaryStage.y =y
        }

        if (maximized) {
            primaryStage.isMaximized = true
        }

        primaryStage.requestFocus()
    }

    private fun importFile(file: File) {
        try {
            mainController.importLogFile(file.readText())
        } catch (e: Exception) {
            logger.w { "Cannot import log file '${file.absolutePath}'" }
        }
    }
}
