package de.robolab.updater

import com.sun.javafx.application.LauncherImpl
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.App


class MainApp : App(MainView::class) {

    override fun start(stage: Stage) {
        stage.initStyle(StageStyle.UTILITY)
        stage.isResizable = false
        stage.sizeToScene()

        stage.properties["params"] = parameters.raw

        super.start(stage)

        stage.toFront()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            LauncherImpl.launchApplication(MainApp::class.java, MainAppPreloader::class.java, args)
        }
    }
}
