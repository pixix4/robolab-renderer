package de.robolab.drawable

import de.robolab.drawable.utils.Utils
import de.robolab.model.Planet
import de.robolab.renderer.Animator
import de.robolab.renderer.DrawContext
import de.robolab.renderer.Plotter
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import kotlin.math.PI
import kotlin.math.max

class SenderDrawable : AnimatableManager<Pair<Int, Int>, SenderDrawable.SenderAnimatable>() {

    class SenderAnimatable(
            reference: Pair<Int, Int>,
            colors: List<Color>
    ) : Animatable<Pair<Int, Int>>(reference) {

        var oldColors: List<Color> = emptyList()
        var newColors: List<Color> = colors
        override val animator = Animator(Plotter.ANIMATION_TIME / 2, Plotter.ANIMATION_TIME / 2)

        private val position = Point(reference.first.toDouble(), reference.second.toDouble())

        override fun startExitAnimation(onFinish: () -> Unit) {
            oldColors = newColors
            newColors = emptyList()
            animator.animate(0.0, 1.0, offset = Plotter.ANIMATION_TIME / 2).onFinish {
                onFinish()
            }
        }

        override fun startEnterAnimation(onFinish: () -> Unit) {
            animator.animate(0.0, 1.0, offset = Plotter.ANIMATION_TIME / 2).onFinish {
                onFinish()
            }
        }

        override fun onDraw(context: DrawContext) {
            val length = max(newColors.size, oldColors.size)

            for (index in 0..length) {
                val p = when (index % 4) {
                    0 -> Point(Plotter.POINT_SIZE / 2.5, Plotter.POINT_SIZE / 2.5)
                    1 -> Point(-Plotter.POINT_SIZE / 2.5, Plotter.POINT_SIZE / 2.5)
                    2 -> Point(-Plotter.POINT_SIZE / 2.5, -Plotter.POINT_SIZE / 2.5)
                    3 -> Point(Plotter.POINT_SIZE / 2.5, -Plotter.POINT_SIZE / 2.5)
                    else -> Point(0.0, 0.0)
                }

                val newColor = newColors.getOrNull(index)
                val oldColor = oldColors.getOrNull(index)

                val steps = (length - (index % 4)) / 4 + 1
                val step = index / 4
                val extend = (PI / 2 - step * PI / (steps * 2.0))

                if (newColor != null) {
                    if (oldColor != null) {
                        satellite(context, position + p, index * PI / 2, extend, oldColor.interpolate(newColor, animator.current))
                    } else {
                        satellite(context, position + p, index * PI / 2, extend * animator.current, newColor)
                    }
                } else if (oldColor != null) {
                    satellite(context, position + p, index * PI / 2, extend * (1.0 - animator.current), oldColor)
                }
            }
        }

        private fun satellite(context: DrawContext, position: Point, start: Double, extend: Double = 90.0, color: Color) {
            context.strokeArc(position, Plotter.TARGET_RADIUS * 0.4, start, extend, color, Plotter.LINE_WIDTH)
            context.strokeArc(position, Plotter.TARGET_RADIUS * 0.7, start, extend, color, Plotter.LINE_WIDTH)
            context.strokeArc(position, Plotter.TARGET_RADIUS * 1.0, start, extend, color, Plotter.LINE_WIDTH)
        }

        override fun startUpdateAnimation(obj: Pair<Int, Int>, planet: Planet) {
            val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
                Utils.getColorByIndex(i)
            }

            animator.animate(0.0, 1.0, offset = Plotter.ANIMATION_TIME / 4)

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
