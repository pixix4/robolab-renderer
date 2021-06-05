package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.view.component.SenderView
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.Planet

class SenderAnimatable(
    reference: Data,
    grouping: List<SenderGrouping>
) : Animatable<SenderAnimatable.Data>(reference) {

    override val view = SenderView(reference.coordinate.point, grouping.map { it to reference.targets.map { it.point } })

    override fun onUpdate(obj: Data, planet: Planet) {
        super.onUpdate(obj, planet)

        val colors= planet.senderGroupingsMap.filterKeys { obj.coordinate in it }.values.toList().map { SenderGrouping(it.first()) }
        view.setColors(colors.map { it to obj.targets.map { it.point } })
    }

    data class Data(
        val coordinate: PlanetPoint,
        val targets: List<PlanetPoint>
    )
}
