package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.utils.Utils
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class SenderAnimatableManager : AnimatableManager<Coordinate, SenderAnimatable>() {

    override fun getObjectList(planet: Planet): List<Coordinate> {
        return Utils.getSenderGrouping(planet).keys.flatten().distinct()
    }

    override fun createAnimatable(obj: Coordinate, planet: Planet): SenderAnimatable {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        val colors = senderGrouping.filterKeys { obj in it }.values.toList()

        return SenderAnimatable(obj, colors)
    }
}
