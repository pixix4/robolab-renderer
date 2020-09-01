package de.robolab.client.updater

import javafx.application.Preloader
import javafx.stage.Stage

class UpdaterAppPreloader: Preloader() {
    override fun start(primaryStage: Stage?) {
        com.sun.glass.ui.Application.GetApplication().setName("Robolab Renderer - Updater");
    }
}
