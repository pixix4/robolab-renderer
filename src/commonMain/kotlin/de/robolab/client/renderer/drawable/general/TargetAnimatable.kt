package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.Utils
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.CircleView
import de.robolab.common.planet.Planet
import de.robolab.common.planet.TargetPoint
import de.robolab.common.utils.Color

class TargetAnimatable(
    reference: TargetPoint,
    initColor: Color
) : Animatable<TargetPoint>(reference) {

    override val view = CircleView(
        reference.target.toPoint(),
        PlottingConstraints.TARGET_RADIUS,
        ViewColor.c(initColor)
    )

    override fun onUpdate(obj: TargetPoint, planet: Planet) {
        super.onUpdate(obj, planet)

        val color =
            Utils.getSenderGrouping(planet)[Utils.getTargetExposure(obj, planet)]?.let { Utils.getColorByIndex(it) }
                ?: Color.TRANSPARENT

        view.setColor(ViewColor.c(color))
    }
}
