package de.robolab.drawable

import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter

class PathListDrawable(
        private val plotter: DefaultPlotter
) : AnimatableManager<Path, PathDrawable>() {

    override fun getObjectList(planet: Planet): List<Path> {
        return planet.pathList
    }

    override fun createAnimatable(obj: Path, planet: Planet): PathDrawable {
        return PathDrawable(obj, plotter)
    }
}
