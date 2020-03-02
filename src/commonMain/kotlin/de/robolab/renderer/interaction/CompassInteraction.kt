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

class CompassInteraction(
        private val transformation: Transformation
) : ICanvasListener {

    private var transitionMap = emptyMap<GenericTransition<*>, () -> Unit>()

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for ((animatable, lambda) in transitionMap) {
            if (animatable.update(ms_offset)) {
                lambda()
                hasChanges = true
            }
        }

        return hasChanges
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
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
            return true
        }

        return false
    }
}
