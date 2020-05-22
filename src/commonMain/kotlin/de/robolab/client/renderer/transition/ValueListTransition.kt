package de.robolab.client.renderer.transition

class ValueListTransition<T : IInterpolatable<T>>(
        initialValue: List<T>
) : GenericListTransition<T>(initialValue, { from, to, progress ->
    from.interpolate(to, progress)
})
