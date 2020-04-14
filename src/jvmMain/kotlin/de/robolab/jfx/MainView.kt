package de.robolab.jfx

import de.robolab.app.controller.MainController
import de.robolab.jfx.view.*
import javafx.application.Platform
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.system.exitProcess

class MainView : View() {

    private val mainController = MainController()

    override val root = hbox {
        title = headerText

        Platform.runLater {
            requestFocus()
        }

        add(SideBar(mainController.sideBarController))

        vbox {
            hgrow = Priority.ALWAYS
            val toolBar = ToolBar(mainController.toolBarController)
            add(toolBar)
            hbox {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                add(MainCanvas(mainController.canvasController))

                val infoBar = InfoBar(mainController.infoBarController, toolBar.infoBarActiveProperty)
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
            add(StatusBar(mainController.statusBarController))
        }
    }

    override fun onUndock() {
        exitProcess(0)
    }

    companion object {
        const val headerText: String = "robolab-renderer"
    }
}
