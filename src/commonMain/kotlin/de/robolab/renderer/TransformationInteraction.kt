package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.*
import kotlin.math.PI

class TransformationInteraction(
        private val plotter: DefaultPlotter
) : ICanvasListener {
    private val transformation = plotter.transformation

    private var lastPoint: Point = Point.ZERO
    var lastDimension: Dimension = Dimension.ZERO
    private var hasMovedSinceDown = false

    override fun onPointerDown(event: PointerEvent): Boolean {
        val position = plotter.updatePointer(event.point)

        if (position != null && plotter.drawable.onPointerDown(event, position)) {
            return true
        }

        lastPoint = event.point
        hasMovedSinceDown = false

        return true
    }

    override fun onPointerUp(event: PointerEvent): Boolean {
        val position = plotter.updatePointer(event.point)

        event.hasMoved = hasMovedSinceDown
        hasMovedSinceDown = false
        val returnValue = position != null && plotter.drawable.onPointerUp(event, position)

        lastPoint = event.point

        return returnValue
    }

    override fun onPointerMove(event: PointerEvent): Boolean {
        val position = plotter.updatePointer(event.point)

        if (position != null && plotter.drawable.onPointerMove(event, position)) {
            return true
        }

        lastPoint = event.point

        return false
    }

    override fun onPointerDrag(event: PointerEvent): Boolean {
        val position = plotter.updatePointer(event.point)

        event.hasMoved = hasMovedSinceDown
        hasMovedSinceDown = true
        if (position != null && plotter.drawable.onPointerDrag(event, position)) {
            return true
        }

        transformation.translateBy(event.point - lastPoint)
        lastPoint = event.point
        return true
    }

    override fun onPointerSecondaryAction(event: PointerEvent): Boolean {
        val position = plotter.updatePointer(event.point)

        if (position != null && plotter.drawable.onPointerSecondaryAction(event, position)) {
            return true
        }

        lastPoint = event.point
        return false
    }

    override fun onPointerEnter(event: PointerEvent): Boolean {
        plotter.updatePointer(event.point)

        lastPoint = event.point
        return false
    }

    override fun onPointerLeave(event: PointerEvent): Boolean {
        plotter.updatePointer(null)

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
        lastDimension = size
        transformation.onViewChange.emit(Unit)
        plotter.drawable.onResize(size)
        plotter.forceRedraw = true

        return true
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        if (plotter.drawable.onKeyPress(event)) {
            return true
        }

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
                    transformation.rotateBy(-KEYBOARD_ROTATION, lastDimension / 2, ANIMATION_TIME)
                } else {
                    transformation.translateBy(Point(KEYBOARD_TRANSLATION, 0.0), ANIMATION_TIME)
                }
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_RIGHT -> {
                if (event.altKey) {
                    transformation.rotateBy(KEYBOARD_ROTATION, lastDimension / 2, ANIMATION_TIME)
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
