package de.robolab.web.views

import de.robolab.app.controller.CanvasController
import de.robolab.web.adapter.WebCanvas
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.Canvas
import kotlin.browser.window

class MainCanvas(
        canvasController: CanvasController,
        sideBarActiveProperty: ReadOnlyProperty<Boolean>,
        infoBarActiveProperty: ReadOnlyProperty<Boolean>
) : ViewCollection<View>() {

    private val canvas = Canvas()
    private val webCanvas = WebCanvas(canvas)

    private var lastSize = clientWidth to clientHeight

    private fun checkSizeChange(@Suppress("UNUSED_PARAMETER") msOffset: Double) {
        val newSize = clientWidth to clientHeight

        if (newSize != lastSize) {
            lastSize = newSize
            canvas.updateSize()
        }

        window.requestAnimationFrame(this::checkSizeChange)
    }

    init {
        classList.bind("side-bar-active", sideBarActiveProperty)
        classList.bind("info-bar-active", infoBarActiveProperty)

        +canvas

        canvasController.setupCanvas(webCanvas)

        checkSizeChange(0.0)
    }
}