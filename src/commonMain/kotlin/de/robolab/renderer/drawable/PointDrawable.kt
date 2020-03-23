package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.IInterpolatable
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle

class PointDrawable(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<Pair<Int, Int>, PointDrawable.PointAnimatable>() {

    data class PointColor(
            val red: Double = 0.0,
            val blue: Double = 0.0,
            val grey: Double = 0.0
    ) : IInterpolatable<PointColor> {

        companion object {
            val RED = PointColor(red = 1.0)
            val BLUE = PointColor(blue = 1.0)
            val GREY = PointColor(grey = 1.0)
        }

        fun toColor(context: DrawContext): Color {
            return Color.mix(mapOf(
                    context.theme.redColor to red,
                    context.theme.blueColor to blue,
                    context.theme.gridTextColor to grey
            ))
        }

        override fun interpolate(toValue: PointColor, progress: Double) = PointColor(
                red * (1 - progress) + toValue.red * progress,
                blue * (1 - progress) + toValue.blue * progress,
                grey * (1 - progress) + toValue.grey * progress
        )
    }

    inner class PointAnimatable(
            reference: Pair<Int, Int>,
            planet: Planet
    ) : Animatable<Pair<Int, Int>>(reference) {

        private val isThisPointEven = (reference.first + reference.second) % 2 == 0
        private val position = Point(reference.first.toDouble(), reference.second.toDouble())

        private val colorTransition = ValueTransition(calcColor(planet))
        private val sizeTransition = DoubleTransition(0.0)
        private val alphaTransition = DoubleTransition(0.0)

        override val animators = listOf(
                colorTransition,
                sizeTransition,
                alphaTransition
        )

        override fun onDraw(context: DrawContext) {
            val size = Point(PlottingConstraints.POINT_SIZE / 2, PlottingConstraints.POINT_SIZE / 2) * sizeTransition.value

            if (reference == planetDrawable.selectedPoint) {
                val selectedSize = size + Point(PlottingConstraints.HOVER_WIDTH, PlottingConstraints.HOVER_WIDTH)

                context.fillRect(Rectangle.fromEdges(
                        position - selectedSize,
                        position + selectedSize
                ), context.theme.highlightColor.a(alphaTransition.value))
            }

            context.fillRect(Rectangle.fromEdges(
                    position - size,
                    position + size
            ), colorTransition.value.toColor(context).a(alphaTransition.value))
        }

        override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
            if (position.distance(this.position) < PlottingConstraints.POINT_SIZE / 2) {
                return listOf(reference)
            }

            return emptyList()
        }

        override fun startExitAnimation(onFinish: () -> Unit) {
            sizeTransition.animate(0.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
            sizeTransition.onFinish.clearListeners()
            sizeTransition.onFinish { onFinish() }

            alphaTransition.animate(0.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            sizeTransition.animate(1.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
            sizeTransition.onFinish.clearListeners()
            sizeTransition.onFinish { onFinish() }

            alphaTransition.animate(1.0, planetDrawable.animationTime / 2, planetDrawable.animationTime / 2)
        }

        override fun startUpdateAnimation(obj: Pair<Int, Int>, planet: Planet) {
            colorTransition.animate(calcColor(planet), this@PointDrawable.planetDrawable.animationTime / 2, this@PointDrawable.planetDrawable.animationTime / 4)
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
    }

    override fun getObjectList(planet: Planet): List<Pair<Int, Int>> {
        return (
                planet.pathList.flatMap { listOf(it.source, it.target) + it.exposure } +
                        planet.targetList.flatMap { it.exposure + it.target } +
                        planet.pathSelectList.map { it.point }
                ).distinct()
    }

    override fun createAnimatable(obj: Pair<Int, Int>, planet: Planet): PointAnimatable {
        return PointAnimatable(obj, planet)
    }
}