package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.live.toAngle
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPathSelect
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Vector

class PathSelectAnimatableManager : AnimatableManager<PlanetPathSelect, PathSelectAnimatable>() {

    override fun getObjectList(planet: Planet) = planet.pathSelects

    override fun createAnimatable(obj: PlanetPathSelect, planet: Planet) = PathSelectAnimatable(obj)

    companion object {
        fun getArrow(position: Vector, direction: PlanetDirection): List<Vector> {
            return listOf(
                    Vector(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6),
                    Vector(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6 + PlottingConstraints.ARROW_LENGTH)
            ).map { it.rotate(direction.toAngle()) + position }
        }
    }
}
