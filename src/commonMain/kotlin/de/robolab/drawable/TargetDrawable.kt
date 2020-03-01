package de.robolab.drawable

import de.robolab.drawable.utils.Utils
import de.robolab.model.Planet
import de.robolab.model.Target
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.GenericTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import kotlin.math.PI

class TargetDrawable : AnimatableManager<Target, TargetDrawable.TargetAnimatable>() {

    class TargetAnimatable(
            override var reference: Target,
            private  val initColor: Color
    ) : Animatable<Target>(reference) {

        private val position = Point(reference.target.first.toDouble(), reference.target.second.toDouble())

        private val colorTransition = ValueTransition(Color.TRANSPARENT)
        private val sizeTransition = DoubleTransition(0.0)

        override val animators = listOf(
                colorTransition,
                sizeTransition
        )

        override fun startExitAnimation(animationTime: Double, onFinish: () -> Unit) {
            sizeTransition.animate(0.0, animationTime / 2, animationTime / 2)
            sizeTransition.onFinish.clearListeners()
            sizeTransition.onFinish { onFinish() }

            colorTransition.animate(Color.TRANSPARENT, animationTime / 2, animationTime / 2)
        }

        override fun startEnterAnimation(animationTime: Double, onFinish: () -> Unit) {
            sizeTransition.animate(1.0, animationTime / 2, animationTime / 2)
            sizeTransition.onFinish.clearListeners()
            sizeTransition.onFinish { onFinish() }


            colorTransition.animate(initColor, animationTime / 2, animationTime / 2)
        }

        override fun onDraw(context: DrawContext) {
            context.fillArc(
                    position,
                    PlottingConstraints.TARGET_RADIUS * sizeTransition.value,
                    0.0,
                    PI * 2,
                    colorTransition.value
            )
        }

        override fun startUpdateAnimation(obj: Target, planet: Planet) {
            val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
                Utils.getColorByIndex(i)
            }

            sizeTransition.animate(1.0, planet.animationTime / 2, planet.animationTime / 2)
            colorTransition.animate(senderGrouping[obj.exposure]
                    ?: Color.TRANSPARENT, planet.animationTime / 2, planet.animationTime / 4)
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
                color ?: Color.TRANSPARENT
        )
    }
}
