package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.dimension

class RotateEvent(
    mousePoint: Vector,
    val angle: Double,
    screen: Dimension,
    ctrlKey: Boolean = false,
    altKey: Boolean = false,
    shiftKey: Boolean = false
) : PointerEvent(mousePoint, screen, ctrlKey, altKey, shiftKey) {

    override fun clip(clip: Rectangle): RotateEvent {
        return RotateEvent(
            mousePoint - clip.topLeft,
            angle,
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

        other as RotateEvent

        if (angle != other.angle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + angle.hashCode()
        return result
    }
}
