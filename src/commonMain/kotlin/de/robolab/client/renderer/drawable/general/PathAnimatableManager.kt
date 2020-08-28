package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.common.planet.Path
import de.robolab.common.planet.Planet

class PathAnimatableManager(
        private val editCallback: IEditCallback? = null
) : AnimatableManager<Path, PathAnimatable>() {

    override fun getObjectList(planet: Planet): List<Path> {
        if (planet.startPoint == null) {
            return planet.pathList
        }

        return planet.pathList + planet.startPoint.path
    }

    override fun createAnimatable(obj: Path, planet: Planet): PathAnimatable {
        return PathAnimatable(obj, planet, editCallback)
    }

    override fun objectEquals(oldValue: Path, newValue: Path): Boolean {
        return oldValue.equalPath(newValue)
    }
}
