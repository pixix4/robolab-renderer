package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.Utils
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.SenderView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Color

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
