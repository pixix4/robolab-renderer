package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Planet

class SenderAnimatableManager : AnimatableManager<SenderAnimatable.Data, SenderAnimatable>() {

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGroupingsMap != newPlanet.senderGroupingsMap
    }

    override fun getObjectList(planet: Planet): List<SenderAnimatable.Data> {
        return planet.senderGroupingsMap.keys.flatten().distinct().map { coordinate ->
            SenderAnimatable.Data(coordinate, planet.targets.filter { coordinate in it.exposure }.map { it.point })
        }
    }

    override fun createAnimatable(obj: SenderAnimatable.Data, planet: Planet): SenderAnimatable {
        val senderGrouping = planet.senderGroupingsMap

        val groupings = senderGrouping.filterKeys { obj.coordinate in it }.values.toList().map { SenderGrouping(it.first()) }

        return SenderAnimatable(obj, groupings)
    }
}
