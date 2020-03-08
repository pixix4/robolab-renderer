package de.robolab.renderer

import de.robolab.renderer.data.Point

data class Pointer(
        val position: Point = Point.ZERO,
        val objectsUnderPointer: List<Any> = emptyList()
) {
    inline fun <reified T: Any> findObjectUnderPointer(): T? {
        for (elem in objectsUnderPointer) {
            if (elem is T) {
                return elem
            }
        }

        return null
    }
}
