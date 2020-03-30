package de.robolab.renderer.drawable

import de.robolab.model.Coordinate
import de.robolab.model.Planet
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.IInterpolatable
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle

class PointDrawable(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<PointDrawable.AttributePoint, PointDrawable.PointAnimatable>() {

    class AttributePoint(
            val coordinate: Coordinate,
            val hidden: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AttributePoint

            if (coordinate != other.coordinate) return false

            return true
        }

        override fun hashCode(): Int {
            return coordinate.hashCode()
        }
    }

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
            reference: AttributePoint,
            planet: Planet
    ) : Animatable<AttributePoint>(reference) {

        private val position = Point(reference.coordinate)

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

            if (reference.coordinate == planetDrawable.selectedPoint) {
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

            if (reference.hidden) {
                context.strokeRect(
                        rect.expand(-PlottingConstraints.LINE_WIDTH / 2),
                        colorTransition.value.toColor(context).a(alphaTransition.value),
                        PlottingConstraints.LINE_WIDTH
                )
            } else {
                context.fillRect(rect, colorTransition.value.toColor(context).a(alphaTransition.value))
            }
        }

        override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
            if (position.distance(this.position) < PlottingConstraints.POINT_SIZE / 2) {
                return listOf(reference.coordinate)
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

        override fun startUpdateAnimation(obj: AttributePoint, planet: Planet) {
            startEnterAnimation { }
            reference = obj
            colorTransition.animate(calcColor(planet), this@PointDrawable.planetDrawable.animationTime / 2, this@PointDrawable.planetDrawable.animationTime / 4)
        }

        private fun calcColor(planet: Planet): PointColor {
            return when (reference.coordinate.getColor(planet.bluePoint)) {
                Coordinate.Color.RED -> PointColor.RED
                Coordinate.Color.BLUE -> PointColor.BLUE
                Coordinate.Color.UNKNOWN -> PointColor.GREY
            }
        }
    }

    override fun getObjectList(planet: Planet): List<AttributePoint> {
        return (
                planet.pathList.flatMap { listOf(it.source, it.target) + it.exposure } +
                        planet.targetList.flatMap { listOf(it.exposure, it.target) } +
                        planet.pathSelectList.map { it.point }
                ).distinct().map { point ->
                    val pathList = planet.pathList.filter { it.source == point || it.target == point }
                    AttributePoint(point, pathList.all { it.hidden })
                }
    }

    override fun createAnimatable(obj: AttributePoint, planet: Planet): PointAnimatable {
        return PointAnimatable(obj, planet)
    }
}
