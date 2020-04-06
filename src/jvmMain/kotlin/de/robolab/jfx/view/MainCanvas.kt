package de.robolab.jfx.view

import de.robolab.app.controller.CanvasController
import de.robolab.jfx.adapter.FxCanvas
import javafx.scene.layout.Priority
import tornadofx.*

class MainCanvas(canvasController: CanvasController) : View() {

    private val canvas = FxCanvas()

    override val root = hbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        add(canvas.canvas)
        canvas.canvas.widthProperty().bind(widthProperty())
        canvas.canvas.heightProperty().bind(heightProperty())
    }

    init {
        canvasController.setupCanvas(canvas)
    }
}
