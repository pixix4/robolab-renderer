package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.dimension

class ScrollEvent(
    mousePoint: Vector,
    val delta: Vector,
    screen: Dimension,
    ctrlKey: Boolean = false,
    altKey: Boolean = false,
    shiftKey: Boolean = false
) : PointerEvent(mousePoint, screen, ctrlKey, altKey, shiftKey) {

    override fun clip(clip: Rectangle): ScrollEvent {
        return ScrollEvent(
            mousePoint - clip.topLeft,
            delta,
            clip.dimension,
            ctrlKey,
            altKey,
            shiftKey
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as ScrollEvent

        if (delta != other.delta) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + delta.hashCode()
        return result
    }
}
