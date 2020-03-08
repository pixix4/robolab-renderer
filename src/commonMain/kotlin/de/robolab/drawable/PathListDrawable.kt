package de.robolab.drawable

import de.robolab.model.Path
import de.robolab.model.Planet

class PathListDrawable(
        private val planet: PlanetDrawable
) : AnimatableManager<Path, PathDrawable>() {

    override fun getObjectList(planet: Planet): List<Path> {
        return planet.pathList
    }

    override fun createAnimatable(obj: Path, planet: Planet): PathDrawable {
        return PathDrawable(obj, this.planet)
    }
}
