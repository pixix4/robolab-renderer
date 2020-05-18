package de.robolab.renderer.drawable.base

import de.robolab.planet.Planet
import de.robolab.renderer.document.GroupView

abstract class AnimatableManager<T: Any, A : Animatable<T>> {

    private val animatableMap = mutableMapOf<T, A>()

    abstract fun getObjectList(planet: Planet): List<T>
    abstract fun createAnimatable(obj: T, planet: Planet): A

    val view = GroupView(this::class.simpleName)

    open fun objectEquals(p1: T, p2: T): Boolean {
        return p1 == p2
    }
    
    open fun importPlanet(planet: Planet) {
        val newReferenceList = getObjectList(planet)

        val updateMap = animatableMap.keys.associateWith { p ->
            newReferenceList.firstOrNull { objectEquals(it, p) }
        }

        val objectsToCreate = newReferenceList - updateMap.values.filterNotNull()
        val objectsToDelete = updateMap.filterValues { it == null }.keys

        for (o in objectsToDelete) {
            val animatable = animatableMap.remove(o)
            animatable?.onDestroy(view)
        }

        for ((old, n) in updateMap) {
            val new = n ?: continue
            val animatable = animatableMap.remove(old) ?: continue
            animatable.onUpdate(new, planet)
            animatableMap[new] = animatable
        }

        for (o in objectsToCreate) {
            val animatable = createAnimatable(o, planet)
            animatableMap[o] = animatable
            animatable.onCreate(view)
        }
    }
}
