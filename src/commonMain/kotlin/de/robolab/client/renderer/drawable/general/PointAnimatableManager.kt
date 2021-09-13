package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.utils.PlanetRequestContext
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue

class PointAnimatableManager(
    private val editCallback: IEditCallback? = null,
    private val requestContext: PlanetRequestContext,
) : AnimatableManager<PointAnimatableManager.AttributePoint, PointAnimatable>() {

    class AttributePoint(
        val coordinate: PlanetPoint,
        val hidden: Boolean,
    ) : IPlanetValue<AttributePoint> {
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

        override fun toString(): String {
            return "AttributePoint(coordinate=$coordinate, hidden=$hidden)"
        }
    }

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return true
    }

    override fun getObjectList(planet: Planet): List<AttributePoint> {
        return planet.getPointList().map { point ->
            AttributePoint(point, isPointHidden(planet, point))
        }
    }

    override fun createAnimatable(obj: AttributePoint, planet: Planet): PointAnimatable {
        return PointAnimatable(obj, planet, editCallback, requestContext)
    }

    companion object {
        fun isPointHidden(planet: Planet, point: PlanetPoint): Boolean {
            var hidden = point != planet.startPoint.point
            hidden = hidden && planet.paths.asSequence().filter { it.connectsWith(point) }.all { it.hidden }
            return hidden
        }
    }
}
