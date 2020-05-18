package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.animation.IInterpolatable
import de.robolab.renderer.data.Color
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.utils.DrawContext

class PointAnimatableManager : AnimatableManager<PointAnimatableManager.AttributePoint, PointAnimatable>() {

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
        return PointAnimatable(obj, planet)
    }
}
