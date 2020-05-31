package de.robolab.client.renderer.events

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.dimension

class ZoomEvent(
    mousePoint: Point,
    val zoomFactor: Double,
    screen: Dimension,
    ctrlKey: Boolean = false,
    altKey: Boolean = false,
    shiftKey: Boolean = false
) : PointerEvent(mousePoint, screen, ctrlKey, altKey, shiftKey) {

    override fun clip(clip: Rectangle): ZoomEvent {
        return ZoomEvent(
            mousePoint - clip.topLeft,
            zoomFactor,
            clip.dimension,
            ctrlKey,
            altKey,
            shiftKey
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ZoomEvent) return false
        if (!super.equals(other)) return false

        if (zoomFactor != other.zoomFactor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + zoomFactor.hashCode()
        return result
    }
}
