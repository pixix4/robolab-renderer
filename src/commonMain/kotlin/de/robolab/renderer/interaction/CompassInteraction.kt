package de.robolab.renderer.interaction

import de.robolab.drawable.CompassDrawable
import de.robolab.renderer.Transformation
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.platform.MouseEvent
import kotlin.math.PI

class CompassInteraction(
        private val transformation: Transformation
) : ICanvasListener {

    override fun onMouseClick(event: MouseEvent): Boolean {
        val compassCenter = Point(
                event.screen.width - CompassDrawable.RIGHT_PADDING,
                CompassDrawable.TOP_PADDING
        )

        if (event.point.distance(compassCenter) <= CompassDrawable.RADIUS) {
            transformation.rotateTo((transformation.rotation - PI) % (2 * PI) + PI, event.screen / 2)
            transformation.rotateTo(0.0, event.screen / 2, 250.0)
            return true
        }

        return false
    }

    override fun onMouseDown(event: MouseEvent): Boolean {
        val compassCenter = Point(
                event.screen.width - CompassDrawable.RIGHT_PADDING,
                CompassDrawable.TOP_PADDING
        )

        return event.point.distance(compassCenter) <= CompassDrawable.RADIUS
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        val compassCenter = Point(
                event.screen.width - CompassDrawable.RIGHT_PADDING,
                CompassDrawable.TOP_PADDING
        )

        return event.point.distance(compassCenter) <= CompassDrawable.RADIUS
    }
}
