package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.Utils
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint
import de.robolab.common.utils.Color

class TargetAnimatableManager : AnimatableManager<TargetPoint, TargetAnimatable>() {

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
            color ?: Color.TRANSPARENT
        )
    }
}
