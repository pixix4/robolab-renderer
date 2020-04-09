package de.westermann.kwebview.components

import de.robolab.utils.runAsync
import de.westermann.kobserve.event.EventHandler
import de.westermann.kwebview.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.MediaQueryList
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.ceil

/**
 * Represents a html label element.
 *
 * @author lars
 */
class Canvas() : View(createHtmlView<HTMLCanvasElement>()) {

    override val html = super.html as HTMLCanvasElement

    val context = html.getContext("2d") as CanvasRenderingContext2D

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
        // Copy image to prevent screen flickering
        val tempCanvas = document.createElement("canvas") as HTMLCanvasElement
        tempCanvas.width = context.canvas.width
        tempCanvas.height = context.canvas.height
        val tempContext = tempCanvas.getContext("2d") as CanvasRenderingContext2D
        tempContext.drawImage(context.canvas, 0.0, 0.0)

        // Resize canvas
        html.width = ceil(width * dpi).toInt()
        html.height = ceil(height * dpi).toInt()
        html.style.width = "${width}px"
        html.style.height = "${height}px"

        // Redraw cached image
        context.drawImage(tempContext.canvas, 0.0, 0.0);

        // Apply transformations
        context.scale(dpi, dpi)
        context.translate(0.5, 0.5)

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
