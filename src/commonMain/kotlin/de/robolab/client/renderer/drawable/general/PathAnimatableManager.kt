package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.utils.PlanetRequestContext
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.Planet

class PathAnimatableManager(
    private val editCallback: IEditCallback? = null,
    private val requestContext: PlanetRequestContext
) : AnimatableManager<PlanetPath, PathAnimatable>() {

    override fun getObjectList(planet: Planet): List<PlanetPath> {
        return planet.paths + planet.startPoint.path
    }

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        return oldPlanet.senderGroupings != newPlanet.senderGroupings
    }

    override fun createAnimatable(obj: PlanetPath, planet: Planet): PathAnimatable {
        return PathAnimatable(obj, planet, editCallback, requestContext)
    }

    override fun objectEquals(oldValue: PlanetPath, newValue: PlanetPath): Boolean {
        return oldValue.equalPath(newValue)
    }
}
