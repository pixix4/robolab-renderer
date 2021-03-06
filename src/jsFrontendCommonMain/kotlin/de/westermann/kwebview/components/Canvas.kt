package de.westermann.kwebview.components

import de.robolab.client.utils.runAsync
import de.westermann.kobserve.event.EventHandler
import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.createHtmlView
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.MediaQueryList
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.ceil

/**
 * Represents a html label element.
 *
 * @author lars
 */
class Canvas : View(createHtmlView<HTMLCanvasElement>()) {

    override val html = super.html as HTMLCanvasElement

    val context = html.getContext("2d", js("{ alpha: false }")) as CanvasRenderingContext2D

    val onResize = EventHandler<Unit>()

    private var lastQuery: MediaQueryList? = null
    private fun eventListener(@Suppress("UNUSED_PARAMETER") event: Event) {
        updateSize()
    }

    fun updateSize() {
        // Get size from parent cause this is fixed via css
        val width = html.parentElement?.clientWidth ?: clientWidth
        val height = html.parentElement?.clientHeight ?: clientHeight

        updateSize(width, height, window.devicePixelRatio)
    }

    var fixedWidth = clientWidth
    var fixedHeight = clientHeight

    fun updateSize(width: Int, height: Int, dpi: Double) {
        if (width <= 0 || height <= 0) return

        // Copy image to prevent screen flickering
        val tempCanvas = document.createElement("canvas") as HTMLCanvasElement
        val useTempCanvas = context.canvas.width > 0.0 && context.canvas.height > 0.0
        if (useTempCanvas) {
            tempCanvas.width = context.canvas.width
            tempCanvas.height = context.canvas.height
            val tempContext = tempCanvas.getContext("2d") as CanvasRenderingContext2D
            tempContext.drawImage(context.canvas, 0.0, 0.0)
        }

        // Resize canvas
        html.width = ceil(width * dpi).toInt()
        html.height = ceil(height * dpi).toInt()
        html.style.width = "${width}px"
        html.style.height = "${height}px"

        if (useTempCanvas) {
            // Redraw cached image
            context.drawImage(tempCanvas, 0.0, 0.0)
        }

        // Apply transformations
        context.setTransform(dpi, 0.0, 0.0, dpi, 0.5, 0.5)
        context.imageSmoothingEnabled = false

        // Update media query listener
        lastQuery?.removeListener(this::eventListener)
        val query = window.matchMedia("(resolution: ${dpi}dppx)")
        query.addListener(this::eventListener)
        lastQuery = query

        fixedWidth = width
        fixedHeight = height

        onResize.emit(Unit)
    }

    init {
        window.addEventListener("resize", object : EventListener {
            override fun handleEvent(event: Event) {
                updateSize()
            }
        })

        runAsync {
            updateSize()
        }
    }
}

@KWebViewDsl
fun ViewCollection<in Canvas>.canvas(init: Canvas.() -> Unit = {}) =
    Canvas().also(this::append).also(init)
