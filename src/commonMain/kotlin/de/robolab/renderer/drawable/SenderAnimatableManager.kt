package de.robolab.renderer.drawable

import de.robolab.model.Coordinate
import de.robolab.model.Planet
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.utils.Utils

class SenderAnimatableManager(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<Coordinate, SenderAnimatable>() {

    override fun getObjectList(planet: Planet): List<Coordinate> {
        return Utils.getSenderGrouping(planet).keys.flatten().distinct()
    }

    override fun createAnimatable(obj: Coordinate, planet: Planet): SenderAnimatable {
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        val colors = senderGrouping.filterKeys { obj in it }.values.toList()

        return SenderAnimatable(obj, colors, planetDrawable)
    }
}
