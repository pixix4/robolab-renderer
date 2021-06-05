package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetTarget

class TargetLabelAnimatableManager : AnimatableManager<PlanetTarget, TargetLabelAnimatable>() {

    override fun getObjectList(planet: Planet): List<PlanetTarget> {
        return planet.targets.distinctBy { it.point }
    }

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGroupingsMap != newPlanet.senderGroupingsMap
    }

    override fun createAnimatable(obj: PlanetTarget, planet: Planet): TargetLabelAnimatable {
        val key = planet.targets.filter { obj.point == it.point }.flatMap { it.exposure }.toSet()
        val grouping = planet.senderGroupingsMap[key] ?: throw IllegalStateException()

        return TargetLabelAnimatable(obj, SenderGrouping(grouping.first()))
    }
}
