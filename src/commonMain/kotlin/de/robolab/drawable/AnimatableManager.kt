package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable

abstract class AnimatableManager<T, A : Animatable<T>> : IDrawable {

    private var animatableMap = mapOf<T, A>()

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

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        for (drawable in animatableMap.values) {
            val obj = drawable.getObjectAtPosition(context, position)
            if (obj != null) {
                return obj
            }
        }

        return null
    }

    abstract fun getObjectList(planet: Planet): List<T>
    abstract fun createAnimatable(obj: T, planet: Planet): A

    fun importPlanet(planet: Planet) {
        val newReferenceList = getObjectList(planet)

        val objectsToDelete = animatableMap.keys - newReferenceList
        val objectsToCreate = newReferenceList - animatableMap.keys

        for (o in objectsToDelete) {
            animatableMap[o]?.let { a ->
                a.startExitAnimation {
                    animatableMap = animatableMap - o
                }
            }
        }

        for (o in animatableMap.keys - objectsToDelete) {
            animatableMap[o]?.startUpdateAnimation(o, planet)
        }

        for (o in objectsToCreate) {
            val a = createAnimatable(o, planet)
            animatableMap = animatableMap + (o to a)
            a.startEnterAnimation {  }
        }
    }
}
