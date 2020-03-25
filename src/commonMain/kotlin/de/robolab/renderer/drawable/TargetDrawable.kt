package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.model.TargetPoint
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
) : AnimatableManager<TargetPoint, TargetDrawable.TargetAnimatable>() {

    inner class TargetAnimatable(
            override var reference: TargetPoint,
            private val initColor: Color
    ) : Animatable<TargetPoint>(reference) {

        private val position = Point(reference.target.x.toDouble(), reference.target.y.toDouble())

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

        override fun startUpdateAnimation(obj: TargetPoint, planet: Planet) {
            val color = Utils.getSenderGrouping(planet)[Utils.getTargetExposure(obj, planet)]?.let { Utils.getColorByIndex(it) }
                    ?: Color.TRANSPARENT

            sizeTransition.animate(1.0, this@TargetDrawable.planetDrawable.animationTime / 2, this@TargetDrawable.planetDrawable.animationTime / 2)
            colorTransition.animate(color, this@TargetDrawable.planetDrawable.animationTime / 2, this@TargetDrawable.planetDrawable.animationTime / 4)
        }
    }

    override fun getObjectList(planet: Planet): List<TargetPoint> {
        return planet.targetList.distinctBy { it.target }
    }

    override fun createAnimatable(obj: TargetPoint, planet: Planet): TargetAnimatable {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        val color = senderGrouping[Utils.getTargetExposure(obj, planet)]

        return TargetAnimatable(
                obj,
                color ?: Color.TRANSPARENT
        )
    }
}
