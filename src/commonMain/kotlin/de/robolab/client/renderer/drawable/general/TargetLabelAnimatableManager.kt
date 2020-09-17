package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint

class TargetLabelAnimatableManager : AnimatableManager<TargetPoint, TargetLabelAnimatable>() {

    override fun getObjectList(planet: Planet): List<TargetPoint> {
        return planet.targetList.distinctBy { it.target }
    }

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGrouping != newPlanet.senderGrouping
    }

    override fun createAnimatable(obj: TargetPoint, planet: Planet): TargetLabelAnimatable {
        val key = planet.targetList.filter { obj.target == it.target }.map { it.exposure }.toSet()
        val grouping = planet.senderGrouping[key] ?: throw IllegalStateException()

        return TargetLabelAnimatable(obj, SenderGrouping(grouping))
    }
}
