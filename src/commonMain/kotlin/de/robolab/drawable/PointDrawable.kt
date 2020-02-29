package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.Animator
import de.robolab.renderer.DrawContext
import de.robolab.renderer.Plotter
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle

class PointDrawable : AnimatableManager<Pair<Int, Int>, PointDrawable.PointAnimatable>() {

    class PointAnimatable(
            reference: Pair<Int, Int>,
            planet: Planet
    ) : Animatable<Pair<Int, Int>>(reference) {
        override val animator = Animator(Plotter.ANIMATION_TIME / 2, Plotter.ANIMATION_TIME / 2)

        private val isThisPointEven = (reference.first + reference.second) % 2 == 0
        private val position = Point(reference.first.toDouble(), reference.second.toDouble())
        private var state = State.UPDATE

        private var oldColor: PointColor = PointColor.GREY
        private var newColor: PointColor = calcColor(planet)

        override fun onDraw(context: DrawContext) {
            var size = Point(Plotter.POINT_SIZE / 2, Plotter.POINT_SIZE / 2)
            var color = newColor.toColor(context)

            if (animator.current < 1.0) when (state) {
                State.ADD -> {
                    size *= animator.current
                    color = newColor.toColor(context).a(animator.current)
                }
                State.UPDATE -> {
                    color = oldColor.toColor(context).interpolate(newColor.toColor(context), animator.current)
                }
                State.REMOVE -> {
                    size *= animator.current
                    color = newColor.toColor(context).a(animator.current)
                }
            }

            context.fillRect(Rectangle.fromEdges(
                    position - size,
                    position + size
            ), color)
        }

        override fun startExitAnimation(onFinish: () -> Unit) {
            state = State.REMOVE
            animator.animate(1.0, 0.0).onFinish { onFinish() }
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            state = State.ADD
            animator.animate(0.0, 1.0).onFinish { onFinish() }
        }

        override fun startUpdateAnimation(obj: Pair<Int, Int>, planet: Planet) {
            oldColor = newColor
            state = State.UPDATE
            newColor = calcColor(planet)
            animator.animate(0.0, 1.0)
        }

        private fun calcColor(planet: Planet): PointColor {
            val planetIsEvenBlue: Boolean? = if (planet.isStartBlue) {
                (planet.startPoint.first + planet.startPoint.second) % 2 == 0
            } else {
                (planet.startPoint.first + planet.startPoint.second) % 2 == 1
            }

            return planetIsEvenBlue?.let {
                if (it == isThisPointEven) {
                    PointColor.BLUE
                } else {
                    PointColor.RED
                }
            } ?: PointColor.GREY
        }


        enum class State {
            ADD, REMOVE, UPDATE
        }

        enum class PointColor {
            RED, BLUE, GREY;

            fun toColor(context: DrawContext) = when (this) {
                RED -> context.theme.redColor
                BLUE -> context.theme.blueColor
                GREY -> context.theme.gridTextColor
            }
        }
    }

    override fun getObjectList(planet: Planet): List<Pair<Int, Int>> {
        return (
                planet.pathList.flatMap { listOf(it.source, it.target) } +
                        planet.targetList.map { it.target } +
                        planet.targetList.flatMap { it.exposure }
                ).distinct()
    }

    override fun createAnimatable(obj: Pair<Int, Int>, planet: Planet): PointAnimatable {
        return PointAnimatable(obj, planet)
    }
}
