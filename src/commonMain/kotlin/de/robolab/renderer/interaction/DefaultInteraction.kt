package de.robolab.renderer.interaction

import de.robolab.renderer.DefaultPlotter
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
    private var lastDimension: Dimension = Dimension.ZERO
    private var isMouseDown = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        plotter.updatePointer(event.point)

        lastPoint = event.point
        isMouseDown = true

        return true
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        plotter.updatePointer(event.point)

        lastPoint = event.point
        isMouseDown = false

        return false
    }

    override fun onMouseMove(event: MouseEvent): Boolean {
        plotter.updatePointer(event.point)
        lastPoint = event.point

        return false
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        plotter.updatePointer(event.point)

        if (!isMouseDown) return false

        transformation.translateBy(event.point - lastPoint)
        lastPoint = event.point
        return true
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        lastPoint = event.point
        return false
    }

    override fun onScroll(event: ScrollEvent): Boolean {
        when {
            event.ctrlKey -> {
                transformation.scaleBy(1.0 + event.delta.top / 40.0 * 0.1, event.point, ANIMATION_TIME)
            }
            event.altKey -> {
                transformation.rotateBy(event.delta.top / 40.0 * PI / 32, event.point, ANIMATION_TIME)
            }
            else -> {
                transformation.translateBy(event.delta, ANIMATION_TIME)
            }
        }

        plotter.updatePointer(event.point)
        lastPoint = event.point

        return true
    }

    override fun onZoom(event: ZoomEvent): Boolean {
        transformation.scaleBy(event.zoomFactor, event.point)

        plotter.updatePointer(event.point)
        lastPoint = event.point

        return true
    }

    override fun onRotate(event: RotateEvent): Boolean {
        transformation.rotateBy(event.angle, event.point)

        plotter.updatePointer(event.point)
        lastPoint = event.point

        return true
    }

    override fun onResize(size: Dimension): Boolean {
        transformation.translateTo(Point(size.width / 2, size.height / 2))
        lastDimension = size

        return true
    }

    override fun onKeyDown(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyCode.ARROW_UP -> {
                transformation.translateBy(Point(0.0, KEYBOARD_TRANSLATION), ANIMATION_TIME)
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_DOWN -> {
                transformation.translateBy(Point(0.0, -KEYBOARD_TRANSLATION), ANIMATION_TIME)
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_LEFT -> {
                if (event.altKey) {
                    transformation.rotateBy(KEYBOARD_ROTATION, lastDimension / 2, ANIMATION_TIME)
                } else {
                    transformation.translateBy(Point(KEYBOARD_TRANSLATION, 0.0), ANIMATION_TIME)
                }
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_RIGHT -> {
                if (event.altKey) {
                    transformation.rotateBy(-KEYBOARD_ROTATION, lastDimension / 2, ANIMATION_TIME)
                } else {
                    transformation.translateBy(Point(-KEYBOARD_TRANSLATION, 0.0), ANIMATION_TIME)
                }
                plotter.updatePointer(lastPoint)
            }
            KeyCode.PLUS, KeyCode.EQUALS -> {
                transformation.scaleBy(KEYBOARD_SCALE, lastDimension / 2.0, ANIMATION_TIME)
                plotter.updatePointer(lastPoint)
            }
            KeyCode.MINUS -> {
                transformation.scaleBy(1.0 / KEYBOARD_SCALE, lastDimension / 2.0, ANIMATION_TIME)
                plotter.updatePointer(lastPoint)
            }
            else -> return false
        }

        return true
    }

    companion object {
        const val KEYBOARD_TRANSLATION = 40.0
        const val KEYBOARD_SCALE = 1.2
        const val KEYBOARD_ROTATION = PI / 16.0

        const val ANIMATION_TIME = 250.0
    }
}
