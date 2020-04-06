package de.robolab.jfx

import de.robolab.app.controller.MainController
import de.robolab.jfx.view.MainCanvas
import de.robolab.jfx.view.SideBar
import de.robolab.jfx.view.StatusBar
import de.robolab.jfx.view.ToolBar
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
            add(ToolBar(mainController.toolBarController))
            add(MainCanvas(mainController.canvasController))
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
