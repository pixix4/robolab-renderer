package de.robolab.client.ui.views.utils

import de.robolab.client.ui.adapter.Hammer
import de.robolab.client.ui.adapter.enablePan
import de.robolab.client.ui.adapter.onPanMove
import de.robolab.client.ui.adapter.onPanStart
import de.robolab.common.utils.Point
import de.westermann.kwebview.View

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
