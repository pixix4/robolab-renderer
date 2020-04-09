package de.robolab.renderer.drawable.general

import de.robolab.planet.Direction
import de.robolab.planet.PathSelect
import de.robolab.planet.Planet
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.utils.DrawContext

class PathSelectAnimatable(
        reference: PathSelect,
        private val animationTime: IAnimationTime
) : Animatable<PathSelect>(reference) {

    private val position = Point(reference.point.x.toDouble(), reference.point.y.toDouble())

    private val transition = DoubleTransition(0.0)
    override val animators = listOf(transition)

    private var oldDirections = emptyList<Direction>()
    private var newDirections = listOf(reference.direction)

    override fun onDraw(context: DrawContext) {
        for (dir in oldDirections - newDirections) {
            val (bottom, top) = PathSelectAnimatableManager.getArrow(position, dir)
            PathAnimatable.drawArrow(context, bottom, top, context.theme.lineColor.a(1 - transition.value))
        }

        for (dir in newDirections - oldDirections) {
            val (bottom, top) = PathSelectAnimatableManager.getArrow(position, dir)
            PathAnimatable.drawArrow(context, bottom, top, context.theme.lineColor.a(transition.value))
        }

        for (dir in newDirections - (newDirections - oldDirections)) {
            val (bottom, top) = PathSelectAnimatableManager.getArrow(position, dir)
            PathAnimatable.drawArrow(context, bottom, top, context.theme.lineColor)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    override fun startExitAnimation(onFinish: () -> Unit) {
        oldDirections = newDirections
        newDirections = emptyList()

        transition.resetValue(0.0)
        transition.animate(1.0, animationTime.animationTime / 2, 0.0)
        transition.onFinish.clearListeners()
        transition.onFinish {
            onFinish()
        }
    }

    override fun startEnterAnimation(onFinish: () -> Unit) {
        transition.resetValue(0.0)
        transition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
        transition.onFinish.clearListeners()
        transition.onFinish {
            onFinish()
        }
    }

    override fun startUpdateAnimation(obj: PathSelect, planet: Planet) {
        reference = obj
        oldDirections = newDirections
        newDirections = listOf(obj.direction)

        transition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 4)
    }
}
