package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.view.component.SenderView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class SenderAnimatable(
    reference: Coordinate,
    grouping: List<SenderGrouping>
) : Animatable<Coordinate>(reference) {

    override val view = SenderView(reference.toPoint(), grouping)

    override fun onUpdate(obj: Coordinate, planet: Planet) {
        super.onUpdate(obj, planet)
        
        val senderGrouping = SenderGrouping.getSenderGrouping(planet)

        view.setColors(senderGrouping.filterKeys { obj in it }.values.toList())
    }
}
