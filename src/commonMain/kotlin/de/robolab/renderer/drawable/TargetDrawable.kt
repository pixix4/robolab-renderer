package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.model.Target
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.utils.Utils
import kotlin.math.PI

class TargetDrawable(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<Target, TargetDrawable.TargetAnimatable>() {

    inner class TargetAnimatable(
            override var reference: Target,
            private val initColor: Color
    ) : Animatable<Target>(reference) {

        private val position = Point(reference.target.first.toDouble(), reference.target.second.toDouble())

        private val colorTransition = ValueTransition(Color.TRANSPARENT)
        private val sizeTransition = DoubleTransition(0.0)

        override val animators = listOf(
                colorTransition,
                sizeTransition
        )

        override fun startExitAnimation(onFinish: () -> Unit) {
            sizeTransition.animate(0.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
            sizeTransition.onFinish.clearListeners()
            sizeTransition.onFinish { onFinish() }

            colorTransition.animate(Color.TRANSPARENT, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            sizeTransition.animate(1.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
            sizeTransition.onFinish.clearListeners()
            sizeTransition.onFinish { onFinish() }

            colorTransition.animate(initColor, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
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

        override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
            return emptyList()
        }

        override fun startUpdateAnimation(obj: Target, planet: Planet) {
            val color = Utils.getSenderGrouping(planet)[obj.exposure]?.let { Utils.getColorByIndex(it) }
                    ?: Color.TRANSPARENT

            sizeTransition.animate(1.0, this@TargetDrawable.planetDrawable.animationTime / 2, this@TargetDrawable.planetDrawable.animationTime / 2)
            colorTransition.animate(color, this@TargetDrawable.planetDrawable.animationTime / 2, this@TargetDrawable.planetDrawable.animationTime / 4)
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