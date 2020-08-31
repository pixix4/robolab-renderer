package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint

class TargetAnimatableManager : AnimatableManager<TargetPoint, TargetAnimatable>() {

    override fun getObjectList(planet: Planet): List<TargetPoint> {
        return planet.targetList.distinctBy { it.target }
    }

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGrouping != newPlanet.senderGrouping
    }

    override fun createAnimatable(obj: TargetPoint, planet: Planet): TargetAnimatable {
        val key = planet.targetList.filter { obj.target == it.target }.map { it.exposure }.toSet()
        val grouping = planet.senderGrouping[key] ?: throw IllegalStateException()

        return TargetAnimatable(obj, SenderGrouping(grouping))
    }
}
