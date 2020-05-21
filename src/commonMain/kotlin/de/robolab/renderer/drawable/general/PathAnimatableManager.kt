package de.robolab.renderer.drawable.general

import de.robolab.planet.Path
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.edit.IEditCallback
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class PathAnimatableManager(
        private val editProperty: ObservableValue<IEditCallback?> = constObservable(null)
) : AnimatableManager<Path, PathAnimatable>() {

    override fun getObjectList(planet: Planet): List<Path> {
        if (planet.startPoint == null) {
            return planet.pathList
        }

        return planet.pathList + planet.startPoint.path
    }

    override fun createAnimatable(obj: Path, planet: Planet): PathAnimatable {
        return PathAnimatable(obj, planet, editProperty)
    }

    override fun objectEquals(oldValue: Path, newValue: Path): Boolean {
        return oldValue.equalPath(newValue)
    }
}
