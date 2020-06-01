package de.robolab.client.renderer.drawable.base

import de.robolab.client.renderer.view.component.GroupView
import de.robolab.common.planet.Planet

abstract class AnimatableManager<T : Any, A : Animatable<T>> {

    private val animatableList = mutableListOf<A>()

    abstract fun getObjectList(planet: Planet): List<T>
    abstract fun createAnimatable(obj: T, planet: Planet): A

    val view = GroupView(this::class.simpleName)

    open fun objectEquals(oldValue: T, newValue: T): Boolean {
        return oldValue == newValue
    }

    protected var objectList: List<T> = emptyList()
    fun importPlanet(planet: Planet) {
        val newReferenceList = getObjectList(planet)
        if (newReferenceList == objectList) {
            // Nothing to do
            return
        }

        val currentAnimatableObjects = animatableList.toList()
        val referenceList = newReferenceList.toMutableList()

        for (animatable in currentAnimatableObjects) {
            val updatedElement = referenceList.find {
                objectEquals(animatable.reference, it)
            }

            if (updatedElement == null) {
                animatableList -= animatable
                animatable.onDestroy(view)
            } else {
                referenceList -= updatedElement
                animatable.onUpdate(updatedElement, planet)
            }
        }

        for (element in referenceList) {
            val animatable = createAnimatable(element, planet)
            animatableList += animatable
            animatable.onCreate(view)
        }

        objectList = newReferenceList
    }
}
