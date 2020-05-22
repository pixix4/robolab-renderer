package de.robolab.client.renderer.transition

class DoubleListTransition(
    initialValue: List<Double>
) : GenericListTransition<Double>(initialValue, { from, to, progress ->
    from + (to - from) * progress
})
