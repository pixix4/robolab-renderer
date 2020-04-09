package de.robolab.renderer.drawable.general

import de.robolab.planet.Direction
import de.robolab.planet.PathSelect
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.live.toAngle

class PathSelectAnimatableManager(
        private val animationTime: IAnimationTime
) : AnimatableManager<PathSelect, PathSelectAnimatable>() {

    override fun getObjectList(planet: Planet) = planet.pathSelectList

    override fun createAnimatable(obj: PathSelect, planet: Planet) = PathSelectAnimatable(obj, animationTime)

    companion object {
        fun getArrow(position: Point, direction: Direction): List<Point> {
            return listOf(
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6),
                    Point(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6 + PlottingConstraints.ARROW_LENGTH)
            ).map { it.rotate(direction.toAngle()) + position }
        }
    }
}
