package de.robolab.renderer.drawable.general

import de.robolab.planet.Coordinate
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.document.SquareView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.utils.toPoint

class PointAnimatable(
        reference: PointAnimatableManager.AttributePoint,
        planet: Planet
) : Animatable<PointAnimatableManager.AttributePoint>(reference) {

    override val view = SquareView(
            reference.coordinate.toPoint(),
            PlottingConstraints.POINT_SIZE,
            PlottingConstraints.LINE_WIDTH * 0.65,
            calcColor(planet),
            !reference.hidden
    )

    override fun onUpdate(obj: PointAnimatableManager.AttributePoint, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setColor(calcColor(planet))
        view.setIsFilled(!obj.hidden)
    }

    private fun calcColor(planet: Planet): ViewColor {
        return when (reference.coordinate.getColor(planet.bluePoint)) {
            Coordinate.Color.RED -> ViewColor.POINT_RED
            Coordinate.Color.BLUE -> ViewColor.POINT_BLUE
            Coordinate.Color.UNKNOWN -> ViewColor.GRID_TEXT_COLOR
        }
    }
}
