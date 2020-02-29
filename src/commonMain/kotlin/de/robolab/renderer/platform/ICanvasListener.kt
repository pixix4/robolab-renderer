package de.robolab.renderer.platform

import de.robolab.renderer.data.Dimension

interface ICanvasListener {

    fun onMouseDown(event: MouseEvent)

    fun onMouseUp(event: MouseEvent)

    fun onMouseMove(event: MouseEvent)

    fun onMouseDrag(event: MouseEvent)

    fun onMouseClick(event: MouseEvent)

    fun onScroll(event: ScrollEvent)

    fun onZoom(event: ZoomEvent)

    fun onRotate(event: RotateEvent)

    fun onResize(size: Dimension)
}
