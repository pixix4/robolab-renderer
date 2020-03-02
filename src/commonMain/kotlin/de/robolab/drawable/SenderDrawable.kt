package de.robolab.drawable

import de.robolab.drawable.utils.Utils
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import kotlin.math.PI
import kotlin.math.max

class SenderDrawable(
        private val plotter: DefaultPlotter
) : AnimatableManager<Pair<Int, Int>, SenderDrawable.SenderAnimatable>() {

    inner class SenderAnimatable(
            reference: Pair<Int, Int>,
            colors: List<Color>
    ) : Animatable<Pair<Int, Int>>(reference) {

        var oldColors: List<Color> = emptyList()
        var newColors: List<Color> = colors

        private val transition = DoubleTransition(0.0)

        override val animators = listOf(transition)

        private val position = Point(reference.first.toDouble(), reference.second.toDouble())

        override fun startExitAnimation(onFinish: () -> Unit) {
            oldColors = newColors
            newColors = emptyList()

            transition.resetValue(0.0)
            transition.animate(1.0, plotter.animationTime / 2, plotter.animationTime / 2)
            transition.onFinish.clearListeners()
            transition.onFinish {
                onFinish()
            }
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            transition.resetValue(0.0)
            transition.animate(1.0, plotter.animationTime / 2, plotter.animationTime / 2)
            transition.onFinish.clearListeners()
            transition.onFinish {
                onFinish()
            }
        }

        override fun onDraw(context: DrawContext) {
            val length = max(newColors.size, oldColors.size)

            for (index in 0..length) {
                val p = when (index % 4) {
                    0 -> Point(PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5)
                    1 -> Point(-PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5)
                    2 -> Point(-PlottingConstraints.POINT_SIZE / 2.5, -PlottingConstraints.POINT_SIZE / 2.5)
                    3 -> Point(PlottingConstraints.POINT_SIZE / 2.5, -PlottingConstraints.POINT_SIZE / 2.5)
                    else -> Point(0.0, 0.0)
                }

                val newColor = newColors.getOrNull(index)
                val oldColor = oldColors.getOrNull(index)

                val steps = (length - (index % 4)) / 4 + 1
                val step = index / 4
                val extend = (PI / 2 - step * PI / (steps * 2.0))

                if (newColor != null) {
                    if (oldColor != null) {
                        satellite(context, position + p, index * PI / 2, extend, oldColor.interpolate(newColor, transition.value))
                    } else {
                        satellite(context, position + p, index * PI / 2, extend * transition.value, newColor)
                    }
                } else if (oldColor != null) {
                    satellite(context, position + p, index * PI / 2, extend * (1.0 - transition.value), oldColor)
                }
            }
        }

        override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
            return null
        }

        private fun satellite(context: DrawContext, position: Point, start: Double, extend: Double = 90.0, color: Color) {
            context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 0.4, start, extend, color, PlottingConstraints.LINE_WIDTH)
            context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 0.7, start, extend, color, PlottingConstraints.LINE_WIDTH)
            context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 1.0, start, extend, color, PlottingConstraints.LINE_WIDTH)
        }

        override fun startUpdateAnimation(obj: Pair<Int, Int>, planet: Planet) {
            val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
                Utils.getColorByIndex(i)
            }

            transition.resetValue(0.0)
            transition.animate(1.0, plotter.animationTime / 2, plotter.animationTime / 4)


            oldColors = newColors
            newColors = senderGrouping.filterKeys { obj in it }.values.toList()
        }

    }

    override fun getObjectList(planet: Planet): List<Pair<Int, Int>> {
        return Utils.getSenderGrouping(planet).keys.flatten().distinct()
    }

    override fun createAnimatable(obj: Pair<Int, Int>, planet: Planet): SenderAnimatable {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        val colors = senderGrouping.filterKeys { obj in it }.values.toList()

        return SenderAnimatable(obj, colors)
    }
}
