package de.westermann.kwebview.components

import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property
import de.westermann.kwebview.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.browser.window

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
        html.width = clientWidth
        html.height = clientHeight

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
