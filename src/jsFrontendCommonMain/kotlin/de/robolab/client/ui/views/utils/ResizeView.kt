package de.robolab.client.ui.views.utils

import de.robolab.client.ui.adapter.Hammer
import de.robolab.client.ui.adapter.enablePan
import de.robolab.client.ui.adapter.onPanMove
import de.robolab.client.ui.adapter.onPanStart
import de.robolab.common.utils.Vector
import de.westermann.kwebview.View

class ResizeView(vararg cssClasses: String, onResize: (position: Vector, size: Vector) -> Unit) : View() {

    private val hammer = Hammer(html, object {})

    private var offset: Vector = Vector.ZERO

    init {
        for (cssClass in cssClasses) {
            classList.add(cssClass)
        }

        hammer.enablePan()

        hammer.onPanStart { event ->
            val source = Vector(offsetLeftTotal, offsetTopTotal)
            val center = event.center.let { Vector(it.x, it.y) }
            offset = center - source
        }

        hammer.onPanMove { event ->
            val center = event.center.let { Vector(it.x, it.y) }
            val size = Vector(clientWidth, clientHeight)
            onResize(center - offset, size)
        }
    }
}
