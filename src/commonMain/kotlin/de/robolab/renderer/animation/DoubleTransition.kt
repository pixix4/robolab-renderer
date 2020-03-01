package de.robolab.renderer.animation

class DoubleTransition(
        initialValue: Double
) : GenericTransition<Double>(initialValue, { from, to, progress ->
    from + (to - from) * progress
})
