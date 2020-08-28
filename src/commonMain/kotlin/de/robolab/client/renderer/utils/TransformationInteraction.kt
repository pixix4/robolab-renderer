package de.robolab.client.renderer.utils

import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.renderer.events.*
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import kotlin.math.PI

class TransformationInteraction(
        private val plotter: PlotterWindow
) : ICanvasListener {
    private val transformation = plotter.transformation

    private var lastPoint: Point = Point.ZERO
    var lastDimension: Dimension = plotter.dimension
    private var hasMovedSinceDown = false

    init {
        transformation.onViewChange {
            plotter.document?.emitOnViewChange()
        }
    }

    var isDrag = false
    override fun onPointerDown(event: PointerEvent) {
        isDrag = false
        val position = plotter.updatePointer(event.mousePoint)
        if (position != null) {
            event.planetPoint = position
        }

        plotter.document?.emitOnPointerDown(event)
        if (position != null && !event.bubbles) {
            return
        }

        isDrag = true
        lastPoint = event.mousePoint
        hasMovedSinceDown = false
    }

    override fun onPointerUp(event: PointerEvent) {
        isDrag = false
        val position = plotter.updatePointer(event.mousePoint)
        if (position != null) {
            event.planetPoint = position
        }

        event.hasMoved = hasMovedSinceDown
        hasMovedSinceDown = false

        plotter.document?.emitOnPointerUp(event)

        lastPoint = event.mousePoint
    }

    override fun onPointerMove(event: PointerEvent) {
        if (isDrag) return
        val position = plotter.updatePointer(event.mousePoint)
        if (position != null) {
            event.planetPoint = position
        }
        
        plotter.document?.emitOnPointerMove(event)
        if (position != null && !event.bubbles) {
            return
        }

        lastPoint = event.mousePoint
    }

    override fun onPointerDrag(event: PointerEvent) {
        val position = plotter.updatePointer(event.mousePoint)
        if (position != null) {
            event.planetPoint = position
        }

        event.hasMoved = hasMovedSinceDown
        hasMovedSinceDown = true

        plotter.document?.emitOnPointerDrag(event)
        if (position != null && !event.bubbles) {
            return
        }

        transformation.translateBy(event.mousePoint - lastPoint)
        plotter.document?.emitOnUserTransformation()

        lastPoint = event.mousePoint
    }

    override fun onPointerSecondaryAction(event: PointerEvent) {
        val position = plotter.updatePointer(event.mousePoint)
        if (position != null) {
            event.planetPoint = position
        }

        plotter.document?.emitOnPointerSecondaryAction(event)
        if (position != null && !event.bubbles) {
            return
        }

        lastPoint = event.mousePoint
    }

    override fun onPointerEnter(event: PointerEvent) {
        isDrag = false
        val position = plotter.updatePointer(event.mousePoint)
        if (position != null) {
            event.planetPoint = position
        }

        lastPoint = event.mousePoint
    }

    override fun onPointerLeave(event: PointerEvent) {
        isDrag = false
        val position = plotter.updatePointer(null)
        if (position != null) {
            event.planetPoint = position
        }
    }

    override fun onScroll(event: ScrollEvent) {
        when {
            event.ctrlKey -> {
                transformation.scaleBy(1.0 + event.delta.top / 40.0 * 0.1, event.mousePoint, ANIMATION_TIME)
            }
            event.altKey -> {
                transformation.rotateBy(event.delta.top / 40.0 * PI / 32, event.mousePoint, ANIMATION_TIME)
            }
            else -> {
                val delta = if (event.shiftKey) event.delta.let { Point(it.y, it.x) } else event.delta
                transformation.translateBy(delta, ANIMATION_TIME)
            }
        }
        plotter.document?.emitOnUserTransformation()

        plotter.updatePointer(event.mousePoint)
        lastPoint = event.mousePoint
    }

    override fun onZoom(event: ZoomEvent) {
        transformation.scaleBy(event.zoomFactor, event.mousePoint)
        plotter.document?.emitOnUserTransformation()

        plotter.updatePointer(event.mousePoint)
        lastPoint = event.mousePoint
    }

    override fun onRotate(event: RotateEvent) {
        transformation.rotateBy(event.angle, event.mousePoint)
        plotter.document?.emitOnUserTransformation()

        plotter.updatePointer(event.mousePoint)
        lastPoint = event.mousePoint
    }

    override fun onResize(size: Dimension) {
        if (lastDimension == size) return

        val oldCenter = transformation.canvasToPlanet(lastDimension / 2)
        val newCenter = transformation.canvasToPlanet(size / 2)
        val diff = (oldCenter - newCenter) * transformation.scaledGridWidth * Point(if (transformation.flipViewProperty.value) 1.0 else -1.0, 1.0)
        transformation.translateBy(diff.rotate(-transformation.rotation))

        lastDimension = size

        plotter.document?.emitOnCanvasResize(size)
        plotter.document?.requestRedraw()
    }

    override fun onKeyPress(event: KeyEvent) {
        plotter.document?.emitOnKeyPress(event)
        if (!event.bubbles) {
            return
        }

        when (event.keyCode) {
            KeyCode.ARROW_UP -> {
                transformation.translateBy(Point(0.0, KEYBOARD_TRANSLATION), ANIMATION_TIME)
                plotter.document?.emitOnUserTransformation()
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_DOWN -> {
                transformation.translateBy(Point(0.0, -KEYBOARD_TRANSLATION), ANIMATION_TIME)
                plotter.document?.emitOnUserTransformation()
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_LEFT -> {
                if (event.altKey) {
                    transformation.rotateBy(-KEYBOARD_ROTATION, lastDimension / 2, ANIMATION_TIME)
                } else {
                    transformation.translateBy(Point(KEYBOARD_TRANSLATION, 0.0), ANIMATION_TIME)
                    plotter.document?.emitOnUserTransformation()
                }
                plotter.updatePointer(lastPoint)
            }
            KeyCode.ARROW_RIGHT -> {
                if (event.altKey) {
                    transformation.rotateBy(KEYBOARD_ROTATION, lastDimension / 2, ANIMATION_TIME)
                } else {
                    transformation.translateBy(Point(-KEYBOARD_TRANSLATION, 0.0), ANIMATION_TIME)
                    plotter.document?.emitOnUserTransformation()
                }
                plotter.updatePointer(lastPoint)
            }
            KeyCode.PLUS, KeyCode.EQUALS -> {
                if (event.shiftKey) {
                    transformation.resetScale(lastDimension / 2.0, ANIMATION_TIME)
                } else {
                    transformation.scaleIn(lastDimension / 2.0, ANIMATION_TIME)
                }
                plotter.updatePointer(lastPoint)
            }
            KeyCode.MINUS -> {
                transformation.scaleOut(lastDimension / 2.0, ANIMATION_TIME)
                plotter.updatePointer(lastPoint)
            }
            else -> return
        }
    }

    override fun onKeyRelease(event: KeyEvent) {
        plotter.document?.emitOnKeyRelease(event)
    }

    companion object {
        const val KEYBOARD_TRANSLATION = 40.0
        const val KEYBOARD_ROTATION = PI / 16.0

        const val ANIMATION_TIME = 250.0
    }
}
