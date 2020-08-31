package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class SenderAnimatableManager : AnimatableManager<Coordinate, SenderAnimatable>() {

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGrouping != newPlanet.senderGrouping
    }

    override fun getObjectList(planet: Planet): List<Coordinate> {
        return planet.senderGrouping.keys.flatten().distinct()
    }

    override fun createAnimatable(obj: Coordinate, planet: Planet): SenderAnimatable {
        val senderGrouping = planet.senderGrouping

        val groupings = senderGrouping.filterKeys { obj in it }.values.toList().map { SenderGrouping(it) }

        return SenderAnimatable(obj, groupings)
    }
}
