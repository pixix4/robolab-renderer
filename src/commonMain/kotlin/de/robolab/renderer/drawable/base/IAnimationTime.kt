package de.robolab.renderer.drawable.base

interface IAnimationTime {

    val animationTime: Double

    val selectedElements: List<Any>
}

inline fun <reified T> IAnimationTime.selectedElements(): List<T> = selectedElements.filterIsInstance<T>()
inline fun <reified T> IAnimationTime.selectedElement(): T? {
    val elements = selectedElements<T>()
    if (elements.size == 1) {
        return elements.first()
    }

    return null
}
