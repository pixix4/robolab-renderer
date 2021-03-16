package de.robolab.client.renderer.utils

interface IRenderInstance {

    fun onRender(msOffset: Double): Boolean
}


inline fun <T> Collection<T>.onRender(msOffset: Double, transform: (T) -> IRenderInstance): Boolean {
    var hasUpdated = false
    for (instance in this) {
        hasUpdated = transform(instance).onRender(msOffset) || hasUpdated
    }
    return hasUpdated
}

fun <T : IRenderInstance> Collection<T>.onRender(msOffset: Double): Boolean {
    return this.onRender(msOffset) { it }
}
