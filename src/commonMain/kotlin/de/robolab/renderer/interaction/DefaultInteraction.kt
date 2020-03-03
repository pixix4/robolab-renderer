package de.robolab.renderer.interaction

import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.Pointer
import de.robolab.renderer.Transformation
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.*
import kotlin.math.PI

class DefaultInteraction(
        private val transformation: Transformation,
        private val plotter: DefaultPlotter
) : ICanvasListener {

    private var lastPoint: Point = Point.ZERO
    private var isMouseDown = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        lastPoint = event.point
        isMouseDown = true
        return true
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        isMouseDown = false
        return false
    }

    override fun onMouseMove(event: MouseEvent): Boolean {
        val pointer = transformation.canvasToPlanet(event.point)
        val element = plotter.getObjectAtPosition(pointer)

        plotter.pointer = Pointer(pointer, element)
        return false
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        val pointer = transformation.canvasToPlanet(event.point)
        val element = plotter.getObjectAtPosition(pointer)

        plotter.pointer = Pointer(pointer, element)

        if (!isMouseDown) return false

        transformation.translateBy(event.point - lastPoint)
        lastPoint = event.point
        return true
    }

    override fun onScroll(event: ScrollEvent): Boolean {
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

        return true
    }

    override fun onZoom(event: ZoomEvent): Boolean {
        transformation.scaleBy(event.zoomFactor, event.point)
        return true
    }

    override fun onRotate(event: RotateEvent): Boolean {
        transformation.rotateBy(event.angle, event.point)
        return true
    }

    override fun onResize(size: Dimension): Boolean {
        transformation.translateTo(Point(size.width / 2, size.height / 2))
        return true
    }
}
