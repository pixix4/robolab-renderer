package de.robolab.jfx

import de.robolab.app.controller.MainController
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.view.*
import javafx.application.Platform
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.system.exitProcess

class MainView : View() {

    private val mainController = MainController()

    override val root = hbox {
        val window = this

        titleProperty.bind(mainController.applicationTitleProperty.toFx())

        Platform.runLater {
            requestFocus()
        }

        val toolBar = ToolBar(mainController.toolBarController)

        val sideBarContainer = hbox {
            val sideBar = SideBar(mainController.sideBarController)

            if (toolBar.sideBarActiveProperty.value) {
                add(sideBar)
            }
            toolBar.sideBarActiveProperty.onChange {
                if (toolBar.sideBarActiveProperty.value) {
                    add(sideBar)
                } else {
                    sideBar.removeFromParent()
                }
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
                hbox {
                    if (toolBar.infoBarActiveProperty.value) {
                        add(infoBar)
                    }
                    toolBar.infoBarActiveProperty.onChange {
                        if (toolBar.infoBarActiveProperty.value) {
                            add(infoBar)
                        } else {
                            infoBar.removeFromParent()
                        }
                    }
                }
            }

            top {
                add(toolBar)
            }
            center {
                val mainCanvas = MainCanvas(mainController.canvasController)

                val centerWidthBinding = prefWidthBinding.subtract(infoBar.root.widthProperty().doubleBinding(toolBar.infoBarActiveProperty.toFx()) {
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
    }

    override fun onUndock() {
        exitProcess(0)
    }

    override fun onDock() {
        super.onDock()

        primaryStage.isMaximized = true
    }
}
