package de.robolab.renderer.interaction

import de.robolab.renderer.Transformation
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.*
import kotlin.math.PI

class DefaultInteraction(private val transformation: Transformation) : ICanvasListener {

    private var lastPoint: Point = Point.ZERO

    override fun onMouseDown(event: MouseEvent) {
        lastPoint = event.point
    }

    override fun onMouseUp(event: MouseEvent) {
    }

    override fun onMouseMove(event: MouseEvent) {
    }

    override fun onMouseDrag(event: MouseEvent) {
        transformation.translateBy(event.point - lastPoint)
        lastPoint = event.point
    }

    override fun onMouseClick(event: MouseEvent) {
    }

    override fun onScroll(event: ScrollEvent) {
        when {
            event.ctrlKey -> {
                transformation.scaleBy(when {
                    event.delta.top > 0 -> 1.1
                    event.delta.top < 0 -> 0.9
                    else -> 1.0
                }, event.point)
            }
            event.altKey -> {
                transformation.rotateBy(when {
                    event.delta.top > 0 -> PI / 32
                    event.delta.top < 0 -> -PI / 32
                    else -> 0.0
                }, event.point)
            }
            else -> {
                transformation.translateBy(event.delta)
            }
        }
    }

    override fun onZoom(event: ZoomEvent) {
        transformation.scaleBy(event.zoomFactor, event.point)
    }

    override fun onRotate(event: RotateEvent) {
        transformation.rotateBy(event.angle, event.point)
    }

    override fun onResize(size: Dimension) {
        transformation.translateTo(Point(size.width / 2, size.height / 2))
    }
}