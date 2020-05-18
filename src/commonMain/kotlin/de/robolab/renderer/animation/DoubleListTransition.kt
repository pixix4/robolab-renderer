package de.robolab.renderer.animation

class DoubleListTransition(
        initialValue: List<Double>
) : GenericListTransition<Double>(initialValue, { from, to, progress ->
    from + (to - from) * progress
})
