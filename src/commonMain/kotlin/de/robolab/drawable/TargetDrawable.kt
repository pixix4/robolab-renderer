package de.robolab.drawable

import de.robolab.drawable.utils.Utils
import de.robolab.model.Planet
import de.robolab.model.Target
import de.robolab.renderer.Animator
import de.robolab.renderer.DrawContext
import de.robolab.renderer.Plotter
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import kotlin.math.PI

class TargetDrawable : AnimatableManager<Target, TargetDrawable.TargetAnimatable>() {

    class TargetAnimatable(
            override var reference: Target,
            color: Color?
    ): Animatable<Target>(reference) {
        override val animator = Animator(Plotter.ANIMATION_TIME / 2, Plotter.ANIMATION_TIME / 2)

        private var oldColor: Color? = null
        private var newColor: Color? = color

        private val position = Point(reference.target.first.toDouble(), reference.target.second.toDouble())

        override fun startExitAnimation(onFinish: () -> Unit) {
            oldColor = newColor
            newColor = null
            animator.animate(0.0, 1.0).onFinish {
                onFinish()
            }
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            animator.animate(0.0, 1.0).onFinish {
                onFinish()
            }
        }

        override fun onDraw(context: DrawContext) {
            val newColor = newColor
            val oldColor = oldColor

            if (newColor != null) {
                if (oldColor != null) {
                    context.fillArc(
                            position,
                            Plotter.TARGET_RADIUS,
                            0.0,
                            PI * 2,
                            oldColor.interpolate(newColor, animator.current)
                    )
                } else {
                    context.fillArc(
                            position,
                            Plotter.TARGET_RADIUS * animator.current,
                            0.0,
                            PI * 2,
                            newColor.a(animator.current)
                    )
                }
            } else if (oldColor != null) {
                context.fillArc(
                        position,
                        Plotter.TARGET_RADIUS * (1 - animator.current),
                        0.0,
                        PI * 2,
                        oldColor.a(1 - animator.current)
                )
            }
        }

        override fun startUpdateAnimation(obj: Target, planet: Planet) {
            val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
                Utils.getColorByIndex(i)
            }

            oldColor = newColor
            newColor = senderGrouping[obj.exposure]

            animator.animate(0.0, 1.0, offset = Plotter.ANIMATION_TIME / 4)
        }

    }

    override fun getObjectList(planet: Planet): List<Target> {
        return planet.targetList
    }

    override fun createAnimatable(obj: Target, planet: Planet): TargetAnimatable {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        val color = senderGrouping[obj.exposure]

        return TargetAnimatable(
                obj,
                color
        )
    }
}
