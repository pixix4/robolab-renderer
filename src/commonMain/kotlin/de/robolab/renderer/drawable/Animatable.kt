package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.renderer.animation.GenericTransition
import de.robolab.renderer.drawable.base.IDrawable

abstract class Animatable<T>(
        open var reference: T
) : IDrawable {

    abstract val animators: List<GenericTransition<*>>

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for (animatable in animators) {
            if (animatable.update(ms_offset)) {
                hasChanges = true
            }
        }

        return hasChanges
    }

    abstract fun startExitAnimation(onFinish: () -> Unit)
    abstract fun startEnterAnimation(onFinish: () -> Unit)
    abstract fun startUpdateAnimation(obj: T, planet: Planet)
}
