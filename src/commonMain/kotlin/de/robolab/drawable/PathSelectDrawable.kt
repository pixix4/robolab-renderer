package de.robolab.drawable

import de.robolab.model.Direction
import de.robolab.model.PathSelect
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point

class PathSelectDrawable : AnimatableManager<PathSelect, PathSelectDrawable.PathSelectAnimatable>() {

    class PathSelectAnimatable(
            override var reference: PathSelect
    ) : Animatable<PathSelect>(reference) {

        private val transition = DoubleTransition(0.0)

        override val animators = listOf(transition)

        private var oldDirections = emptyList<Direction>()
        private var newDirections = listOf(reference.direction)

        private val position = Point(reference.point.first.toDouble(), reference.point.second.toDouble())

        override fun startExitAnimation(animationTime: Double, onFinish: () -> Unit) {
            oldDirections = newDirections
            newDirections = emptyList()

            transition.resetValue(0.0)
            transition.animate(1.0, animationTime / 2, 0.0)
            transition.onFinish.clearListeners()
            transition.onFinish {
                onFinish()
            }
        }

        override fun startEnterAnimation(animationTime: Double, onFinish: () -> Unit) {
            transition.resetValue(0.0)
            transition.animate(1.0, animationTime / 2, animationTime / 2)
            transition.onFinish.clearListeners()
            transition.onFinish {
                onFinish()
            }
        }

        private fun getArrow(direction: Direction): List<Point> {
            val rotation = when (direction) {
                Direction.NORTH -> 0.0
                Direction.EAST -> 0.0
                Direction.SOUTH -> 0.0
                Direction.WEST -> 0.0
            }
            return listOf(
                    Point(PlottingConstraints.POINT_SIZE / 8, PlottingConstraints.POINT_SIZE / 2),
                    Point(3 * PlottingConstraints.POINT_SIZE / 8, PlottingConstraints.POINT_SIZE / 2),
                    Point(PlottingConstraints.POINT_SIZE / 4, PlottingConstraints.POINT_SIZE * 1.25)
            ).map { it.rotate(rotation) + position }
        }

        override fun onDraw(context: DrawContext) {
            for (dir in oldDirections - newDirections) {
                context.fillPolygon(getArrow(dir), context.theme.gridTextColor.a(1 - transition.value))
            }

            for (dir in newDirections - oldDirections) {
                context.fillPolygon(getArrow(dir), context.theme.gridTextColor.a(transition.value))
            }

            for (dir in newDirections - (newDirections - oldDirections)) {
                context.fillPolygon(getArrow(dir), context.theme.gridTextColor)
            }
        }

        override fun startUpdateAnimation(obj: PathSelect, planet: Planet) {
            reference = obj
            oldDirections = newDirections
            newDirections = listOf(obj.direction)

            transition.animate(1.0, planet.animationTime / 2, planet.animationTime / 4)
        }

    }

    override fun getObjectList(planet: Planet) = planet.pathSelectList

    override fun createAnimatable(obj: PathSelect, planet: Planet) = PathSelectAnimatable(obj)
}
