package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.live.toAngle
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.common.planet.Direction
import de.robolab.common.planet.PathSelect
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Point

class PathSelectAnimatableManager : AnimatableManager<PathSelect, PathSelectAnimatable>() {

    override fun getObjectList(planet: Planet) = planet.pathSelectList

    override fun createAnimatable(obj: PathSelect, planet: Planet) = PathSelectAnimatable(obj)

    companion object {
        fun getArrow(position: Point, direction: Direction): List<Point> {
            return listOf(
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6),
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6 + PlottingConstraints.ARROW_LENGTH)
            ).map { it.rotate(direction.toAngle()) + position }
        }
    }
}
