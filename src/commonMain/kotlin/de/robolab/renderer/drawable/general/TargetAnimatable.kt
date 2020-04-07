package de.robolab.renderer.drawable.general

import de.robolab.planet.Planet
import de.robolab.model.TargetPoint
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.utils.Utils
import de.robolab.renderer.utils.DrawContext
import kotlin.math.PI

class TargetAnimatable(
        reference: TargetPoint,
        private val initColor: Color,
        private val animationTime: IAnimationTime
) : Animatable<TargetPoint>(reference) {

    private val position = Point(reference.target.x.toDouble(), reference.target.y.toDouble())

    private val colorTransition = ValueTransition(Color.TRANSPARENT)
    private val sizeTransition = DoubleTransition(0.0)

    override val animators = listOf(
            colorTransition,
            sizeTransition
    )

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

    override fun startExitAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(0.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        colorTransition.animate(Color.TRANSPARENT, animationTime.animationTime / 2, animationTime.animationTime / 2)
    }

    override fun startEnterAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        colorTransition.animate(initColor, animationTime.animationTime / 2, animationTime.animationTime / 2)
    }

    override fun startUpdateAnimation(obj: TargetPoint, planet: Planet) {
        val color = Utils.getSenderGrouping(planet)[Utils.getTargetExposure(obj, planet)]?.let { Utils.getColorByIndex(it) }
                ?: Color.TRANSPARENT

        sizeTransition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
        colorTransition.animate(color, animationTime.animationTime / 2, animationTime.animationTime / 4)
    }
}
