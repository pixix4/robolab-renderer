package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.Animator
import de.robolab.renderer.drawable.IDrawable

abstract class Animatable<T>(
        open val reference: T
): IDrawable {

    abstract val animator: Animator

    override fun onUpdate(ms_offset: Double): Boolean {
        return animator.update(ms_offset)
    }

    abstract fun startExitAnimation(onFinish: () -> Unit)
    abstract fun startEnterAnimation(onFinish: () -> Unit)
    abstract fun startUpdateAnimation(obj: T, planet: Planet)
}
