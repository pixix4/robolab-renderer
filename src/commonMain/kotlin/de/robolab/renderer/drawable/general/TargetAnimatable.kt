package de.robolab.renderer.drawable.general

import de.robolab.planet.Planet
import de.robolab.planet.TargetPoint
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.CircleView
import de.robolab.renderer.document.SquareView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.utils.Utils
import de.robolab.renderer.drawable.utils.toPoint
import de.robolab.renderer.utils.DrawContext
import kotlin.math.PI

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
        
        val color = Utils.getSenderGrouping(planet)[Utils.getTargetExposure(obj, planet)]?.let { Utils.getColorByIndex(it) }
                ?: Color.TRANSPARENT

        view.setColor(ViewColor.c(color))
    }
}
