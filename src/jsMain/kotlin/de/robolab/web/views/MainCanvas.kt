package de.robolab.web.views

import de.robolab.app.controller.CanvasController
import de.robolab.renderer.DefaultPlotter
import de.robolab.web.adapter.WebCanvas
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.Canvas

class MainCanvas(private val canvasController: CanvasController) : ViewCollection<View>() {

    private val canvas = Canvas()
    private val webCanvas = WebCanvas(canvas)

    init {
        +canvas

        canvasController.setupCanvas(webCanvas)
    }
}