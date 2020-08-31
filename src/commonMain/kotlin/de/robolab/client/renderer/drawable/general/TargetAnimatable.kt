package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.CircleView
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint
import de.robolab.common.utils.Color

class TargetAnimatable(
    reference: TargetPoint,
    grouping: SenderGrouping
) : Animatable<TargetPoint>(reference) {

    override val view = CircleView(
        reference.target.toPoint(),
        PlottingConstraints.TARGET_RADIUS,
        ViewColor.c(grouping.color)
    )

    override fun onUpdate(obj: TargetPoint, planet: Planet) {
        super.onUpdate(obj, planet)

        val key = planet.targetList.filter { obj.target == it.target }.map { it.exposure }.toSet()
        val color = planet.senderGrouping[key]?.let { SenderGrouping(it) }?.color
                ?: Color.TRANSPARENT

        view.setColor(ViewColor.c(color))
    }
}
