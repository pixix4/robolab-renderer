package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.edit.CreatePathManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class PointAnimatableManager(
    private val editProperty: ObservableValue<IEditCallback?> = constObservable(null),
    private val createPath: CreatePathManager? = null
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
        return PointAnimatable(obj, planet, editProperty, createPath)
    }
}