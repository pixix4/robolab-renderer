package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class SenderAnimatableManager : AnimatableManager<SenderAnimatable.Data, SenderAnimatable>() {

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGrouping != newPlanet.senderGrouping
    }

    override fun getObjectList(planet: Planet): List<SenderAnimatable.Data> {
        return planet.senderGrouping.keys.flatten().distinct().map { coordinate ->
            SenderAnimatable.Data(coordinate, planet.targetList.filter { it.exposure == coordinate }.map { it.target })
        }
    }

    override fun createAnimatable(obj: SenderAnimatable.Data, planet: Planet): SenderAnimatable {
        val senderGrouping = planet.senderGrouping

        val groupings = senderGrouping.filterKeys { obj.coordinate in it }.values.toList().map { SenderGrouping(it) }

        return SenderAnimatable(obj, groupings)
    }
}
