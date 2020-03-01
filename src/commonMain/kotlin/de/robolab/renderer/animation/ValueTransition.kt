package de.robolab.renderer.animation

class ValueTransition<T : IInterpolatable<T>>(
        initialValue: T
) : GenericTransition<T>(initialValue, { from, to, progress ->
    from.interpolate(to, progress)
})
