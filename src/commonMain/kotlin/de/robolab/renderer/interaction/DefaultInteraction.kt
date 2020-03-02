package de.robolab.renderer.interaction

import de.robolab.drawable.CompassDrawable
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.Pointer
import de.robolab.renderer.Transformation
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.GenericTransition
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.*
import kotlin.math.PI

class DefaultInteraction(
        private val transformation: Transformation,
        private val plotter: DefaultPlotter
) : ICanvasListener {

    private var transitionMap = emptyMap<GenericTransition<*>, () -> Unit>()

    fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for ((animatable, lambda) in transitionMap) {
            if (animatable.update(ms_offset)) {
                lambda()
                hasChanges = true
            }
        }

        return hasChanges
    }

    private var lastPoint: Point = Point.ZERO

    override fun onMouseDown(event: MouseEvent) {
        lastPoint = event.point
    }

    override fun onMouseUp(event: MouseEvent) {
    }

    override fun onMouseMove(event: MouseEvent) {
        val pointer = transformation.canvasToPlanet(event.point)
        val element = plotter.getObjectAtPosition(pointer)

        plotter.pointer = Pointer(pointer, element)
    }

    override fun onMouseDrag(event: MouseEvent) {
        transformation.translateBy(event.point - lastPoint)
        lastPoint = event.point
    }

    override fun onMouseClick(event: MouseEvent) {
        val compassCenter = Point(
                event.screen.width - CompassDrawable.RIGHT_PADDING,
                CompassDrawable.TOP_PADDING
        )

        if (event.point.distance(compassCenter) <= CompassDrawable.RADIUS) {
            val transition = DoubleTransition((transformation.rotation - PI) % (2 * PI) + PI)
            transition.animate(0.0, 250.0)
            transition.onFinish {
                transitionMap = transitionMap - transition
            }
            transitionMap = transitionMap + (transition to {
                transformation.rotateTo(transition.value, Point(
                        event.screen.width / 2,
                        event.screen.height / 2
                ))
            })
        }
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
