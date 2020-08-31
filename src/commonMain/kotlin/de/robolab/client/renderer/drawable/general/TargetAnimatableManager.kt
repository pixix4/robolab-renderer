package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint

class TargetAnimatableManager : AnimatableManager<TargetPoint, TargetAnimatable>() {

    override fun getObjectList(planet: Planet): List<TargetPoint> {
        return planet.targetList.distinctBy { it.target }
    }

    override fun createAnimatable(obj: TargetPoint, planet: Planet): TargetAnimatable {
        val senderGrouping = SenderGrouping.getSenderGrouping(planet)

        val grouping = senderGrouping[SenderGrouping.getTargetExposure(obj, planet)] ?: throw IllegalStateException()

        return TargetAnimatable(obj, grouping)
    }
}
