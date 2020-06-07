package de.robolab.client.jfx

import javafx.application.Preloader
import javafx.stage.Stage

class MainAppPreloader: Preloader() {
    override fun start(primaryStage: Stage?) {
        com.sun.glass.ui.Application.GetApplication().setName("Robolab Renderer");
    }
}
