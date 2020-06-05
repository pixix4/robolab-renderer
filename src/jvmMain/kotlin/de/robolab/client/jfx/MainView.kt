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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*
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

        val navigationBarContainer = stackpane {
            minWidth = 260.0

            add(NavigationBar(mainController.navigationBarController, mainController.fileImportController))

            visibleWhen(toolBar.navigationBarActiveProperty.toFx())
        }

        var lastNavigationBarWidth = 260.0
        val navigationBarWidthProperty = property(object : DelegatePropertyAccessor<Double> {
            override fun set(value: Double) {
                navigationBarContainer.minWidth = value
                navigationBarContainer.prefWidth = value
                navigationBarContainer.maxWidth = value
                if (value > 0.0) {
                    lastNavigationBarWidth = value
                }
            }

            override fun get(): Double {
                return navigationBarContainer.width
            }
        })
        navigationBarContainer.widthProperty().onChange {
            navigationBarWidthProperty.onChange.emit(Unit)
        }
        toolBar.navigationBarActiveProperty.onChange {
            if (toolBar.navigationBarActiveProperty.value) {
                navigationBarWidthProperty.value = lastNavigationBarWidth
            } else {
                navigationBarWidthProperty.value = 0.0
            }
        }


        borderpane {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            val prefWidthBinding = window.widthProperty().subtract(navigationBarContainer.widthProperty())
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
                    toolBar.navigationBarActiveProperty,
                    navigationBarWidthProperty,
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

        setOnDragOver { event ->
            if (event.gestureSource != this &&
                event.dragboard.hasFiles() &&
                event.dragboard.files.any {
                    mainController.fileImportController.isFileSupported(it.name)
                }
            ) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
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
                            mainController.fileImportController.importFile(
                                file.name,
                                file.readText()
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
