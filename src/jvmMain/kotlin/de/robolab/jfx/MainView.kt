package de.robolab.jfx

import de.robolab.app.controller.MainController
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.view.*
import de.westermann.kobserve.property.FunctionAccessor
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.system.exitProcess

class MainView : View() {

    private val mainController = MainController()

    override val root = hbox {
        val window = this

        titleProperty.bind(mainController.applicationTitleProperty.toFx())
        primaryStage.icons.clear()
        primaryStage.icons.add(Image("icon.png"))

        Platform.runLater {
            requestFocus()
        }

        val toolBar = ToolBar(mainController.toolBarController)

        val sideBarContainer = stackpane {
            minWidth = 260.0

            add(SideBar(mainController.sideBarController))

            visibleWhen(toolBar.sideBarActiveProperty.toFx())
        }

        var lastSideBarWidth = 260.0
        val sideBarWidthProperty = de.westermann.kobserve.property.property(object: FunctionAccessor<Double> {
            override fun set(value: Double): Boolean {
                sideBarContainer.minWidth = value
                sideBarContainer.prefWidth = value
                sideBarContainer.maxWidth = value
                if (value > 0.0) {
                    lastSideBarWidth = value
                }
                return true
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
            val infoBarWidthProperty = de.westermann.kobserve.property.property(object: FunctionAccessor<Double> {
                override fun set(value: Double): Boolean {
                    infoBar.root.minWidth = value
                    infoBar.root.prefWidth = value
                    infoBar.root.maxWidth = value
                    if (value > 0.0) {
                        lastInfoBarWidth = value
                    }
                    return true
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
