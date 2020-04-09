package de.robolab.renderer.drawable.general

import de.robolab.planet.Planet
import de.robolab.planet.TargetPoint
import de.robolab.renderer.data.Color
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.utils.Utils

class TargetAnimatableManager(
        private val animationTime: IAnimationTime
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
                animationTime
        )
    }
}
