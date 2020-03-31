package de.westermann.kwebview.components

import de.westermann.kobserve.event.EventHandler
import de.westermann.kwebview.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.browser.window
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Represents a html label element.
 *
 * @author lars
 */
class Canvas() : View(createHtmlView<HTMLCanvasElement>()) {

    override val html = super.html as HTMLCanvasElement

    val context = html.getContext("2d") as CanvasRenderingContext2D

    val onResize = EventHandler<Unit>()

    fun updateSize() {
        val width = html.parentElement?.clientWidth ?: clientWidth
        val height = html.parentElement?.clientHeight ?: clientHeight

        html.width = ceil(width * window.devicePixelRatio).toInt()
        html.height = ceil(height * window.devicePixelRatio).toInt()
        html.style.width = "${width}px"
        html.style.height = "${height}px"

        context.scale(window.devicePixelRatio, window.devicePixelRatio)
        context.translate(0.5, 0.5)

        onResize.emit(Unit)
    }

    init {
        window.addEventListener("resize", object : EventListener {
            override fun handleEvent(event: Event) {
                updateSize()
            }
        })

        async {
            updateSize()
        }
    }
}

@KWebViewDsl
fun ViewCollection<in Canvas>.canvas(init: Canvas.() -> Unit = {}) =
        Canvas().also(this::append).also(init)
