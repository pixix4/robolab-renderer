package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.view.component.SenderCharView
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetTarget

class TargetLabelAnimatable(
    reference: PlanetTarget,
    grouping: SenderGrouping
) : Animatable<PlanetTarget>(reference) {

    override val view = SenderCharView(
        reference.point.point,
        grouping
    )

    override fun onUpdate(obj: PlanetTarget, planet: Planet) {
        super.onUpdate(obj, planet)

        val key = planet.targets.filter { obj.point == it.point }.flatMap { it.exposure }.toSet()
        val grouping = planet.senderGroupingsMap[key]?.let { SenderGrouping(it.first()) } ?: throw IllegalStateException()

        view.setGrouping(grouping)
    }
}
