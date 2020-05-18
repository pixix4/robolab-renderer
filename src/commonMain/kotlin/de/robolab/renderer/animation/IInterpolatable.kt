package de.robolab.renderer.animation

interface IInterpolatable<T : IInterpolatable<T>> {

    fun interpolate(toValue: T, progress: Double): T
    
    fun interpolateToNull(progress: Double): T
    
    fun interpolateFromNull(progress: Double): T {
        return interpolateToNull(1.0 - progress)
    }
}

fun <T: IInterpolatable<T>> nullableInterpolator() : (fromValue: T?, toValue: T?, progress: Double) -> T? {
    return { fromValue, toValue, progress ->
        if (fromValue == null) {
            toValue?.interpolateFromNull(progress)
        } else {
            if (toValue == null) {
                fromValue.interpolateToNull(progress)
            } else {
                fromValue.interpolate( toValue, progress)
            }
        }
    }
}
