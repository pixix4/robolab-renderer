package de.robolab.web.views

import de.robolab.app.controller.CanvasController
import de.robolab.renderer.data.Point
import de.robolab.web.adapter.*
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.Canvas
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.max

class MainCanvas(
        canvasController: CanvasController,
        sideBarActiveProperty: ObservableProperty<Boolean>,
        infoBarActiveProperty: ObservableProperty<Boolean>
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

        +ResizeView("side-bar-resize") { position, _ ->
            var width = position.x
            if (sideBarActiveProperty.value) {
                if (width < 50.0) {
                    sideBarActiveProperty.value = false
                } else {
                    width = max(width, 200.0)
                    document.body?.style?.setProperty("--side-bar-width", "${width}px")
                }
            } else {
                if (width >= 50.0) {
                    sideBarActiveProperty.value = true
                }
            }
        }

        +ResizeView("info-bar-resize") { position, size ->
            var width = window.innerWidth - position.x - size.x
            if (infoBarActiveProperty.value) {
                if (width < 50.0) {
                    infoBarActiveProperty.value = false
                } else {
                    width = max(width, 200.0)
                    document.body?.style?.setProperty("--info-bar-width", "${width}px")
                }
            } else {
                if (width >= 50.0) {
                    infoBarActiveProperty.value = true
                }
            }
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
