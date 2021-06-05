package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.dimension

open class PointerEvent(
    val mousePoint: Vector,
    val screen: Dimension,
    val ctrlKey: Boolean = false,
    val altKey: Boolean = false,
    val shiftKey: Boolean = false
): Event() {
    var planetPoint: Vector = mousePoint
    var hasMoved: Boolean = false

    open fun clip(clip: Rectangle): PointerEvent {
        return PointerEvent(
            mousePoint - clip.topLeft,
            clip.dimension,
            ctrlKey,
            altKey,
            shiftKey
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PointerEvent) return false

        if (mousePoint != other.mousePoint) return false
        if (screen != other.screen) return false
        if (ctrlKey != other.ctrlKey) return false
        if (altKey != other.altKey) return false
        if (shiftKey != other.shiftKey) return false
        if (planetPoint != other.planetPoint) return false
        if (hasMoved != other.hasMoved) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mousePoint.hashCode()
        result = 31 * result + screen.hashCode()
        result = 31 * result + ctrlKey.hashCode()
        result = 31 * result + altKey.hashCode()
        result = 31 * result + shiftKey.hashCode()
        result = 31 * result + planetPoint.hashCode()
        result = 31 * result + hasMoved.hashCode()
        return result
    }

    enum class Type {
        DOWN, UP, DRAG, MOVE
    }
}
