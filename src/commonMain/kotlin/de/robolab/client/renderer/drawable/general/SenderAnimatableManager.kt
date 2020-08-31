package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class SenderAnimatableManager : AnimatableManager<Coordinate, SenderAnimatable>() {

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return true
    }

    override fun getObjectList(planet: Planet): List<Coordinate> {
        return SenderGrouping.getSenderGrouping(planet).keys.flatten().distinct()
    }

    override fun createAnimatable(obj: Coordinate, planet: Planet): SenderAnimatable {
        val senderGrouping = SenderGrouping.getSenderGrouping(planet)

        val groupings = senderGrouping.filterKeys { obj in it }.values.toList()

        return SenderAnimatable(obj, groupings)
    }
}
