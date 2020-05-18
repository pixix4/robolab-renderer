package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.SenderView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.utils.Utils
import de.robolab.renderer.drawable.utils.toPoint
import de.robolab.renderer.utils.DrawContext
import kotlin.math.PI
import kotlin.math.max

class SenderAnimatable(
        reference: Coordinate,
        colors: List<Color>
) : Animatable<Coordinate>(reference) {

    override val view = SenderView(
            reference.toPoint(),
            colors.map {
                ViewColor.c(it)
            }
    )

    override fun onUpdate(obj: Coordinate, planet: Planet) {
        super.onUpdate(obj, planet)
        
        val senderGrouping = Utils.getSenderGrouping(planet).mapValues { (_, i) ->
            Utils.getColorByIndex(i)
        }

        view.setColors(
                senderGrouping.filterKeys { obj in it }.values.toList().map {
                    ViewColor.c(it)
                }
        )
    }
}
