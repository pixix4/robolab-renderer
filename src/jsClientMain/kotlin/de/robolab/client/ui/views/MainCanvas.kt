package de.robolab.client.ui.views

import de.robolab.client.app.controller.CanvasController
import de.robolab.client.ui.adapter.*
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.Canvas
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.max

class MainCanvas(
    canvasController: CanvasController,
    navigationBarActiveProperty: ObservableProperty<Boolean>,
    infoBarActiveProperty: ObservableProperty<Boolean>,
    infoBarWidthProperty: ObservableProperty<Double>
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
        classList.bind("navigation-bar-active", navigationBarActiveProperty)
        classList.bind("info-bar-active", infoBarActiveProperty)

        +canvas

        +ResizeView("navigation-bar-resize") { position, _ ->
            var width = position.x
            if (navigationBarActiveProperty.value) {
                if (width < 50.0) {
                    navigationBarActiveProperty.value = false
                } else {
                    width = max(width, 200.0)
                    document.body?.style?.setProperty("--navigation-bar-width", "${width}px")
                }
            } else {
                if (width >= 50.0) {
                    navigationBarActiveProperty.value = true
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
                    infoBarWidthProperty.value = width
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
