package de.robolab.client.ui.views

import de.robolab.client.app.controller.CanvasController
import de.robolab.client.app.controller.UiController
import de.robolab.client.ui.adapter.*
import de.robolab.common.utils.Point
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.Canvas
import kotlinx.browser.window

class MainCanvas(
    canvasController: CanvasController,
    private val uiController: UiController
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
        +canvas

        +ResizeView("navigation-bar-resize") { position, _ ->
            uiController.setNavigationBarWidth(position.x)
        }

        +ResizeView("info-bar-resize") { position, size ->
            uiController.setInfoBarWidth(window.innerWidth - position.x - size.x)
        }

        canvasController.setupCanvas(webCanvas)

        checkSizeChange(0.0)
    }
}

class ResizeView(vararg cssClasses: String, onResize: (position: Point, size: Point) -> Unit) : View() {

    private val hammer = Hammer(html, object {})

    private var offset: Point = Point.ZERO

    init {
        for (cssClass in cssClasses) {
            classList.add(cssClass)
        }

        hammer.enablePan()

        hammer.onPanStart { event ->
            val source = Point(offsetLeftTotal, offsetTopTotal)
            val center = event.center.let { Point(it.x, it.y) }
            offset = center - source
        }

        hammer.onPanMove { event ->
            val center = event.center.let { Point(it.x, it.y) }
            val size = Point(clientWidth, clientHeight)
            onResize(center - offset, size)
        }
    }
}
