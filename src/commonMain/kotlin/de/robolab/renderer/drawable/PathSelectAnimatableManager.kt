package de.robolab.renderer.drawable

import de.robolab.model.Direction
import de.robolab.model.PathSelect
import de.robolab.model.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.live.toAngle
import kotlin.math.PI

class PathSelectAnimatableManager(
        private val planetDrawable: PlanetDrawable
) : AnimatableManager<PathSelect, PathSelectAnimatable>() {

    override fun getObjectList(planet: Planet) = planet.pathSelectList

    override fun createAnimatable(obj: PathSelect, planet: Planet) = PathSelectAnimatable(obj, planetDrawable)

    companion object {
        fun getArrow(position: Point, direction: Direction): List<Point> {
            return listOf(
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6),
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6 + PlottingConstraints.ARROW_LENGTH)
            ).map { it.rotate(direction.toAngle()) + position }
        }
    }
}
