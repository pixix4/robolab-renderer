package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.utils.DrawContext

class PointAnimatable(
        reference: PointAnimatableManager.AttributePoint,
        planet: Planet,
        private val animationTime: IAnimationTime
) : Animatable<PointAnimatableManager.AttributePoint>(reference) {

    private val position = Point(reference.coordinate)

    private val colorTransition = ValueTransition(calcColor(planet))
    private val sizeTransition = DoubleTransition(0.0)
    private val alphaTransition = DoubleTransition(0.0)
    private val hiddenTransition = DoubleTransition(if (reference.hidden) 1.0 else 0.0)

    override val animators = listOf(
            colorTransition,
            sizeTransition,
            alphaTransition,
            hiddenTransition
    )

    override fun onUpdate(ms_offset: Double): Boolean {
        return super.onUpdate(ms_offset)
    }

    override fun onDraw(context: DrawContext) {
        val size = Point(PlottingConstraints.POINT_SIZE / 2, PlottingConstraints.POINT_SIZE / 2) * sizeTransition.value

        if (reference.coordinate == animationTime.selectedElement<Coordinate>()) {
            val selectedSize = size + Point(PlottingConstraints.HOVER_WIDTH / 2, PlottingConstraints.HOVER_WIDTH / 2)

            val rect = Rectangle.fromEdges(
                    position - selectedSize,
                    position + selectedSize
            )

            context.strokeRect(
                    rect,
                    context.theme.highlightColor.a(alphaTransition.value),
                    PlottingConstraints.HOVER_WIDTH
            )
        }

        val rect = Rectangle.fromEdges(
                position - size,
                position + size
        )

        if (hiddenTransition.value > 0.0) {
            val innerRect = rect.shrink(PlottingConstraints.LINE_WIDTH / 2)
            context.strokeRect(
                    innerRect,
                    colorTransition.value.toColor(context).a(alphaTransition.value),
                    PlottingConstraints.LINE_WIDTH
            )
            if (hiddenTransition.value < 1.0) {
                val strokeWidth = innerRect.width * (1.0 - hiddenTransition.value)
                context.strokeRect(
                        innerRect.shrink(strokeWidth / 2),
                        colorTransition.value.toColor(context).a(alphaTransition.value),
                        strokeWidth
                )
            }
        } else {
            context.fillRect(rect, colorTransition.value.toColor(context).a(alphaTransition.value))
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (position.manhattanDistance(this.position) < PlottingConstraints.POINT_SIZE / 2) {
            return listOf(reference.coordinate)
        }

        return emptyList()
    }

    override fun startExitAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(0.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(0.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
    }

    override fun startEnterAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(1.0, animationTime.animationTime / 2, animationTime.animationTime / 2)
    }

    override fun startUpdateAnimation(obj: PointAnimatableManager.AttributePoint, planet: Planet) {
        startEnterAnimation { }
        reference = obj
        hiddenTransition.animate(if (obj.hidden) 1.0 else 0.0, animationTime.animationTime)
        colorTransition.animate(calcColor(planet), animationTime.animationTime / 2, animationTime.animationTime / 4)
    }

    private fun calcColor(planet: Planet): PointAnimatableManager.PointColor {
        return when (reference.coordinate.getColor(planet.bluePoint)) {
            Coordinate.Color.RED -> PointAnimatableManager.PointColor.RED
            Coordinate.Color.BLUE -> PointAnimatableManager.PointColor.BLUE
            Coordinate.Color.UNKNOWN -> PointAnimatableManager.PointColor.GREY
        }
    }
}
