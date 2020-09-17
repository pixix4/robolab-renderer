package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.view.component.SenderCharView
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint

class TargetLabelAnimatable(
    reference: TargetPoint,
    grouping: SenderGrouping
) : Animatable<TargetPoint>(reference) {

    override val view = SenderCharView(
        reference.target.toPoint(),
        grouping
    )

    override fun onUpdate(obj: TargetPoint, planet: Planet) {
        super.onUpdate(obj, planet)

        val key = planet.targetList.filter { obj.target == it.target }.map { it.exposure }.toSet()
        val grouping = planet.senderGrouping[key]?.let { SenderGrouping(it) } ?: throw IllegalStateException()

        view.setGrouping(grouping)
    }
}
