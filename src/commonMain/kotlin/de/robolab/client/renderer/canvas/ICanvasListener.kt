package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.events.*
import de.robolab.common.utils.Dimension

interface ICanvasListener {

    fun onPointerDown(event: PointerEvent) {}

    fun onPointerUp(event: PointerEvent) {}

    fun onPointerMove(event: PointerEvent) {}

    fun onPointerDrag(event: PointerEvent) {}

    fun onPointerSecondaryAction(event: PointerEvent) {}

    fun onPointerEnter(event: PointerEvent) {}

    fun onPointerLeave(event: PointerEvent) {}

    fun onScroll(event: ScrollEvent) {}

    fun onZoom(event: ZoomEvent) {}

    fun onRotate(event: RotateEvent) {}

    fun onResize(size: Dimension) {}

    fun onKeyPress(event: KeyEvent) {}

    fun onKeyRelease(event: KeyEvent) {}
}
