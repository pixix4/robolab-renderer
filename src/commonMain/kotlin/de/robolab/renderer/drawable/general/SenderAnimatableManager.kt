package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.utils.Utils

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
