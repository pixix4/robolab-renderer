package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.events.*
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.property.property

class VirtualCanvas() : ICanvas {

    val canvasProperty = property<ICanvas>()
    var canvas by canvasProperty

    private val listenerManager = CanvasListenerManager()
    override fun addListener(listener: ICanvasListener) {
        listenerManager += listener
    }

    override fun removeListener(listener: ICanvasListener) {
        listenerManager -= listener
    }

    private var latestDimension = Dimension.ZERO
    private fun updateDimension(): Dimension {
        val newDimension = canvas?.dimension

        if (newDimension != null && newDimension != latestDimension) {
            latestDimension = newDimension
            listenerManager.onResize(newDimension)
        }

        return latestDimension
    }

    override val dimension: Dimension
        get() = updateDimension()

    override fun fillRect(rectangle: Rectangle, color: Color) {
        canvas?.fillRect(rectangle, color)
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        canvas?.strokeRect(rectangle, color, width)
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        canvas?.fillPolygon(points, color)
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        canvas?.strokePolygon(points, color, width)
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        canvas?.strokeLine(points, color, width)
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        canvas?.dashLine(points, color, width, dashes, dashOffset)
    }

    override fun fillText(
        text: String,
        position: Point,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        canvas?.fillText(text, position, color, fontSize, alignment, fontWeight)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas?.fillArc(center, radius, startAngle, extendAngle, color)
    }

    override fun strokeArc(
        center: Point,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
        canvas?.strokeArc(center, radius, startAngle, extendAngle, color, width)
    }

    override fun openContextMenu(menu: ContextMenu) {
        canvas?.openContextMenu(menu)
    }

    override fun startClip(rectangle: Rectangle) {
        canvas?.startClip(rectangle)
    }

    override fun endClip() {
        canvas?.endClip()
    }

    private val backingCanvasListener = object : ICanvasListener {
        override fun onPointerDown(event: PointerEvent) {
            listenerManager.onPointerDown(event)
        }

        override fun onPointerUp(event: PointerEvent) {
            listenerManager.onPointerUp(event)
        }

        override fun onPointerMove(event: PointerEvent) {
            listenerManager.onPointerMove(event)
        }

        override fun onPointerDrag(event: PointerEvent) {
            listenerManager.onPointerDrag(event)
        }

        override fun onPointerSecondaryAction(event: PointerEvent) {
            listenerManager.onPointerSecondaryAction(event)
        }

        override fun onPointerEnter(event: PointerEvent) {
            listenerManager.onPointerEnter(event)
        }

        override fun onPointerLeave(event: PointerEvent) {
            listenerManager.onPointerLeave(event)
        }

        override fun onScroll(event: ScrollEvent) {
            listenerManager.onScroll(event)
        }

        override fun onZoom(event: ZoomEvent) {
            listenerManager.onZoom(event)
        }

        override fun onRotate(event: RotateEvent) {
            listenerManager.onRotate(event)
        }

        override fun onResize(size: Dimension) {
            updateDimension()
        }

        override fun onKeyPress(event: KeyEvent) {
            listenerManager.onKeyPress(event)
        }

        override fun onKeyRelease(event: KeyEvent) {
            listenerManager.onKeyRelease(event)
        }
    }

    init {
        var oldCanvas: ICanvas? = null
        canvasProperty.onChange {
            oldCanvas?.removeListener(backingCanvasListener)
            oldCanvas = canvas
            oldCanvas?.addListener(backingCanvasListener)

            updateDimension()
        }
    }
}
