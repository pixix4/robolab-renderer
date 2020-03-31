package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.model.TargetPoint
import de.robolab.renderer.data.Color
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.utils.Utils

class TargetAnimatableManager(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<TargetPoint, TargetAnimatable>() {

    override fun getObjectList(planet: Planet): List<TargetPoint> {
        return planet.targetList.distinctBy { it.target }
    }

    override fun createAnimatable(obj: TargetPoint, planet: Planet): TargetAnimatable {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        val color = senderGrouping[Utils.getTargetExposure(obj, planet)]

        return TargetAnimatable(
                obj,
                color ?: Color.TRANSPARENT,
                planetDrawable
        )
    }
}
