package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.view.component.SenderView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class SenderAnimatable(
    reference: Data,
    grouping: List<SenderGrouping>
) : Animatable<SenderAnimatable.Data>(reference) {

    override val view = SenderView(reference.coordinate.toPoint(), grouping.map { it to reference.targets.map { it.toPoint() } })

    override fun onUpdate(obj: Data, planet: Planet) {
        super.onUpdate(obj, planet)

        val colors= planet.senderGrouping.filterKeys { obj.coordinate in it }.values.toList().map { SenderGrouping(it) }
        view.setColors(colors.map { it to obj.targets.map { it.toPoint() } })
    }

    data class Data(
        val coordinate: Coordinate,
        val targets: List<Coordinate>
    )
}
