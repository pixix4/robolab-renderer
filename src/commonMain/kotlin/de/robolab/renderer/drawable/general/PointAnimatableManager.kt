package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.animation.IInterpolatable
import de.robolab.renderer.data.Color
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.utils.DrawContext

class PointAnimatableManager(
        private val animationTime: IAnimationTime
) : AnimatableManager<PointAnimatableManager.AttributePoint, PointAnimatable>() {

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
                    context.theme.plotter.redColor to red,
                    context.theme.plotter.blueColor to blue,
                    context.theme.plotter.gridTextColor to grey
            ))
        }

        override fun interpolate(toValue: PointColor, progress: Double) = PointColor(
                red * (1 - progress) + toValue.red * progress,
                blue * (1 - progress) + toValue.blue * progress,
                grey * (1 - progress) + toValue.grey * progress
        )
    }

    private fun isPointHidden(point: Coordinate, planet: Planet): Boolean {
        var hidden = point != planet.startPoint?.point
        hidden = hidden && planet.pathList.asSequence().filter { it.connectsWith(point) }.all { it.hidden }
        return hidden
    }

    override fun getObjectList(planet: Planet): List<AttributePoint> {
        return (
                planet.pathList.flatMap { listOf(it.source, it.target) + it.exposure } +
                        planet.targetList.flatMap { listOf(it.exposure, it.target) } +
                        planet.pathSelectList.map { it.point } +
                        listOfNotNull(planet.startPoint?.point)
                ).distinct().map { point ->
                    AttributePoint(point, isPointHidden(point, planet))
                }
    }

    override fun createAnimatable(obj: AttributePoint, planet: Planet): PointAnimatable {
        return PointAnimatable(obj, planet, animationTime)
    }
}
