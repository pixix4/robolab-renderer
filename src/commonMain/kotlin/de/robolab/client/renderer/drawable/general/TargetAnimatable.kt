package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.CircleView
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetTarget
import de.robolab.common.utils.Color

class TargetAnimatable(
    reference: PlanetTarget,
    grouping: SenderGrouping
) : Animatable<PlanetTarget>(reference) {

    override val view = CircleView(
        reference.point.point,
        PlottingConstraints.TARGET_RADIUS,
        ViewColor.c(grouping.color)
    )

    override fun onUpdate(obj: PlanetTarget, planet: Planet) {
        super.onUpdate(obj, planet)

        val key = planet.targets.filter { obj.point == it.point }.flatMap { it.exposure }.toSet()
        val color = planet.senderGroupingsMap[key]?.let { SenderGrouping(it.first()) }?.color
                ?: Color.TRANSPARENT

        view.setColor(ViewColor.c(color))
    }
}
