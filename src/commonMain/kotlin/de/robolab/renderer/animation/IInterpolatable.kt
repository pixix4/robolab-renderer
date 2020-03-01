package de.robolab.renderer.animation

interface IInterpolatable<T: IInterpolatable<T>> {
    fun interpolate(toValue: T, progress: Double): T
}
