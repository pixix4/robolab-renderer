package de.robolab.client.updater

import com.sun.javafx.application.LauncherImpl
import de.robolab.client.ui.MainApp
import de.robolab.client.ui.dialog.UpdateDialog
import de.robolab.client.ui.style.StylesheetLoader
import de.robolab.client.ui.style.SystemTheme
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.UpdateChannel
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.utils.ConfigFile
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.App
import java.nio.file.Paths


class UpdaterApp : App(UpdaterView::class) {

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
            MainApp.setupSystemProperties()
            MainApp.setupTheme()
            LauncherImpl.launchApplication(UpdaterApp::class.java, UpdaterAppPreloader::class.java, args)
        }
    }
}
