package de.robolab.client.renderer.transition

class DoubleTransition(
        initialValue: Double
) : GenericTransition<Double>(initialValue, { from, to, progress ->
    from + (to - from) * progress
})
