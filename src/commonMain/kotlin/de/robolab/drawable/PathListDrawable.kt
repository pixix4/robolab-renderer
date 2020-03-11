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

    override fun importPlanet(planet: Planet) {
        val newReferenceList = getObjectList(planet)
        val updateMap = animatableMap.keys.associateWith { p ->
            newReferenceList.firstOrNull { it.equalPath(p) }
        }

        val objectsToCreate = newReferenceList - updateMap.values.filterNotNull()
        val objectsToDelete = updateMap.filterValues { it == null }.keys

        for (o in objectsToDelete) {
            animatableMap[o]?.let { a ->
                a.startExitAnimation {
                    animatableMap = animatableMap - o
                }
            }
        }

        for ((old, n) in updateMap) {
            val new = n ?: continue
            val drawable = animatableMap[old] ?: continue
            animatableMap = animatableMap - old
            animatableMap = animatableMap + (new to drawable)
            drawable.startUpdateAnimation(new, planet)
        }

        for (o in objectsToCreate) {
            val a = createAnimatable(o, planet)
            animatableMap = animatableMap + (o to a)
            a.startEnterAnimation { }
        }
    }
}
