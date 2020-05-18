package de.robolab.renderer.animation

class ValueListTransition<T : IInterpolatable<T>>(
        initialValue: List<T>
) : GenericListTransition<T>(initialValue, { from, to, progress ->
    from.interpolate(to, progress)
})
