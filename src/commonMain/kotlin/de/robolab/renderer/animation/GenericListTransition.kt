package de.robolab.renderer.animation

open class GenericListTransition<T>(
        initialValue: List<T>,
        interpolate: (from: T, to: T, progress: Double) -> T
) : GenericTransition<List<T>>(initialValue, { from, to, progress ->
    if (from.size == to.size) {
        from.zip(to) { f, t ->
            interpolate(f, t, progress)
        }
    } else {
        to // TODO: nice animation
    }
})
