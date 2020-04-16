package de.robolab.jfx.view

import de.robolab.app.controller.CanvasController
import de.robolab.jfx.adapter.FxCanvas
import javafx.scene.layout.Priority
import tornadofx.*

class MainCanvas(canvasController: CanvasController) : View() {

    private val canvas = FxCanvas()

    override val root = hbox {
        minWidth = 0.0
        minHeight = 0.0

        add(canvas.canvas)
        canvas.canvas.widthProperty().bind(widthProperty())
        canvas.canvas.heightProperty().bind(heightProperty())
    }

    init {
        canvasController.setupCanvas(canvas)
    }
}
