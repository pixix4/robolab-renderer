package de.robolab.drawable

import de.robolab.model.Direction
import de.robolab.model.PathSelect
import de.robolab.model.Planet
import de.robolab.renderer.Animator
import de.robolab.renderer.DrawContext
import de.robolab.renderer.Plotter
import de.robolab.renderer.data.Point

class PathSelectDrawable : AnimatableManager<PathSelect, PathSelectDrawable.PathSelectAnimatable>() {

    class PathSelectAnimatable(
            override var reference: PathSelect
    ) : Animatable<PathSelect>(reference) {
        override val animator = Animator(Plotter.ANIMATION_TIME / 2, Plotter.ANIMATION_TIME / 2)

        private var oldDirections = emptyList<Direction>()
        private var newDirections = listOf(reference.direction)

        private val position = Point(reference.point.first.toDouble(), reference.point.second.toDouble())

        override fun startExitAnimation(onFinish: () -> Unit) {
            oldDirections = newDirections
            newDirections = emptyList()
            animator.animate(0.0, 1.0).onFinish {
                onFinish()
            }
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            animator.animate(0.0, 1.0).onFinish {
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
                    Point(Plotter.POINT_SIZE / 8, Plotter.POINT_SIZE / 2),
                    Point(3 * Plotter.POINT_SIZE / 8, Plotter.POINT_SIZE / 2),
                    Point(Plotter.POINT_SIZE / 4, Plotter.POINT_SIZE * 1.25)
            ).map { it.rotate(rotation) + position }
        }

        override fun onDraw(context: DrawContext) {
            for (dir in oldDirections - newDirections) {
                context.fillPolygon(getArrow(dir), context.theme.gridTextColor.a(1 - animator.current))
            }

            for (dir in newDirections - oldDirections) {
                context.fillPolygon(getArrow(dir), context.theme.gridTextColor.a(animator.current))
            }

            for (dir in newDirections - (newDirections - oldDirections)) {
                context.fillPolygon(getArrow(dir), context.theme.gridTextColor)
            }
        }

        override fun startUpdateAnimation(obj: PathSelect, planet: Planet) {
            reference = obj
            oldDirections = newDirections
            newDirections = listOf(obj.direction)

            animator.animate(0.0, 1.0, offset = Plotter.ANIMATION_TIME / 4)
        }

    }

    override fun getObjectList(planet: Planet) = planet.pathSelectList

    override fun createAnimatable(obj: PathSelect, planet: Planet) = PathSelectAnimatable(obj)
}
