package de.robolab.renderer.drawable.base

import de.robolab.model.Planet
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.data.Point

abstract class AnimatableManager<T, A : Animatable<T>> : IDrawable {

    protected var animatableMap = mapOf<T, A>()

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for (animatable in animatableMap.values) {
            if (animatable.onUpdate(ms_offset)) {
                hasChanges = true
            }
        }

        return hasChanges
    }

    override fun onDraw(context: DrawContext) {
        for (animatable in animatableMap.values) {
            animatable.onDraw(context)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return animatableMap.values.flatMap { it.getObjectsAtPosition(context, position) }
    }

    abstract fun getObjectList(planet: Planet): List<T>
    abstract fun createAnimatable(obj: T, planet: Planet): A

    open fun importPlanet(planet: Planet) {
        val newReferenceList = getObjectList(planet)

        val objectsToDelete = animatableMap.keys - newReferenceList
        val objectsToCreate = newReferenceList - animatableMap.keys
        
        val updateMap = (animatableMap.keys - objectsToDelete).associateWith { o1 ->
            newReferenceList.find { o1 == it } ?: o1
        }

        for (o in objectsToDelete) {
            animatableMap[o]?.let { a ->
                a.startExitAnimation {
                    animatableMap = animatableMap - o
                }
            }
        }

        for ((old, new) in updateMap) {
            val elem = animatableMap[old] ?: continue
            animatableMap - old
            elem.startUpdateAnimation(new, planet)
            animatableMap = animatableMap + (new to elem)
        }

        for (o in objectsToCreate) {
            val a = createAnimatable(o, planet)
            animatableMap = animatableMap + (o to a)
            a.startEnterAnimation { }
        }
    }
}
