package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.utils.Utils
import de.robolab.renderer.utils.DrawContext
import kotlin.math.PI
import kotlin.math.max

class SenderAnimatable(
        reference: Coordinate,
        colors: List<Color>,
        private val animationTime: IAnimationTime
) : Animatable<Coordinate>(reference) {

    private val position = Point(reference.x.toDouble(), reference.y.toDouble())

    private var oldColors: List<Color> = emptyList()
    private var newColors: List<Color> = colors

    private val transition = DoubleTransition(0.0)
    override val animators = listOf(transition)

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

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    private fun satellite(context: DrawContext, position: Point, start: Double, extend: Double = 90.0, color: Color) {
        context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 0.4, start, extend, color, PlottingConstraints.LINE_WIDTH)
        context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 0.7, start, extend, color, PlottingConstraints.LINE_WIDTH)
        context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 1.0, start, extend, color, PlottingConstraints.LINE_WIDTH)
    }

    override fun startExitAnimation(onFinish: () -> Unit) {
        oldColors = newColors
        newColors = emptyList()

        transition.resetValue(0.0)
        transition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
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

    override fun startUpdateAnimation(obj: Coordinate, planet: Planet) {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        transition.resetValue(0.0)
        transition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 4)


        oldColors = newColors
        newColors = senderGrouping.filterKeys { obj in it }.values.toList()
    }
}
