package de.robolab.renderer.drawable

import de.robolab.model.Direction
import de.robolab.model.PathSelect
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point
import kotlin.math.PI

class PathSelectDrawable(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<PathSelect, PathSelectDrawable.PathSelectAnimatable>() {

    inner class PathSelectAnimatable(
            override var reference: PathSelect
    ) : Animatable<PathSelect>(reference) {

        private val transition = DoubleTransition(0.0)

        override val animators = listOf(transition)

        private var oldDirections = emptyList<Direction>()
        private var newDirections = listOf(reference.direction)

        private val position = Point(reference.point.x.toDouble(), reference.point.y.toDouble())

        override fun startExitAnimation(onFinish: () -> Unit) {
            oldDirections = newDirections
            newDirections = emptyList()

            transition.resetValue(0.0)
            transition.animate(1.0, planetDrawable.animationTime / 2, 0.0)
            transition.onFinish.clearListeners()
            transition.onFinish {
                onFinish()
            }
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            transition.resetValue(0.0)
            transition.animate(1.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
            transition.onFinish.clearListeners()
            transition.onFinish {
                onFinish()
            }
        }

        override fun onDraw(context: DrawContext) {
            for (dir in oldDirections - newDirections) {
                val (bottom, top) = getArrow(position, dir)
                PathDrawable.drawArrow(context, bottom, top, context.theme.lineColor.a(1 - transition.value))
            }

            for (dir in newDirections - oldDirections) {
                val (bottom, top) = getArrow(position, dir)
                PathDrawable.drawArrow(context, bottom, top, context.theme.lineColor.a(transition.value))
            }

            for (dir in newDirections - (newDirections - oldDirections)) {
                val (bottom, top) = getArrow(position, dir)
                PathDrawable.drawArrow(context, bottom, top, context.theme.lineColor)
            }
        }

        override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
            return emptyList()
        }

        override fun startUpdateAnimation(obj: PathSelect, planet: Planet) {
            reference = obj
            oldDirections = newDirections
            newDirections = listOf(obj.direction)

            transition.animate(1.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 4)
        }

    }

    override fun getObjectList(planet: Planet) = planet.pathSelectList

    override fun createAnimatable(obj: PathSelect, planet: Planet) = PathSelectAnimatable(obj)

    companion object {
        fun getArrow(position: Point, direction: Direction): List<Point> {
            val rotation = when (direction) {
                Direction.NORTH -> 0.0
                Direction.WEST -> PI / 2
                Direction.SOUTH -> PI
                Direction.EAST -> 3 * PI / 2
            }
            return listOf(
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6),
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6 + PlottingConstraints.ARROW_LENGTH)
            ).map { it.rotate(rotation) + position }
        }
    }
}
