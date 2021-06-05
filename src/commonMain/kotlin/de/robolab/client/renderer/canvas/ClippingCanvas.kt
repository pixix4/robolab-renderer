package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.events.*
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.*

class ClippingCanvas(private val canvas: ICanvas, initClip: Rectangle) : ICanvas by canvas {

    var clip = initClip
        set(value) {
            field = value
            listenerManager.onResize(value.dimension)
        }

    private val listenerManager = CanvasListenerManager()
    override fun addListener(listener: ICanvasListener) {
        listenerManager += listener
    }

    override fun removeListener(listener: ICanvasListener) {
        listenerManager -= listener
    }

    val transformListener = object : ICanvasListener {
        override fun onPointerDown(event: PointerEvent) {
            listenerManager.onPointerDown(event.clip(clip))
        }

        override fun onPointerUp(event: PointerEvent) {
            listenerManager.onPointerUp(event.clip(clip))
        }

        override fun onPointerMove(event: PointerEvent) {
            listenerManager.onPointerMove(event.clip(clip))
        }

        override fun onPointerDrag(event: PointerEvent) {
            listenerManager.onPointerDrag(event.clip(clip))
        }

        override fun onPointerSecondaryAction(event: PointerEvent) {
            listenerManager.onPointerSecondaryAction(event.clip(clip))
        }

        override fun onPointerEnter(event: PointerEvent) {
            listenerManager.onPointerEnter(event.clip(clip))
        }

        override fun onPointerLeave(event: PointerEvent) {
            listenerManager.onPointerLeave(event.clip(clip))
        }

        override fun onScroll(event: ScrollEvent) {
            listenerManager.onScroll(event.clip(clip))
        }

        override fun onZoom(event: ZoomEvent) {
            listenerManager.onZoom(event.clip(clip))
        }

        override fun onRotate(event: RotateEvent) {
            listenerManager.onRotate(event.clip(clip))
        }

        override fun onKeyPress(event: KeyEvent) {
            listenerManager.onKeyPress(event)
        }

        override fun onKeyRelease(event: KeyEvent) {
            listenerManager.onKeyRelease(event)
        }

        override fun onResize(size: Dimension) {
            listenerManager.onResize(clip.dimension)
        }
    }

    override val dimension: Dimension
        get() = clip.dimension

    override fun fillRect(rectangle: Rectangle, color: Color) {
        canvas.fillRect(rectangle.removeClip(), color)
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        canvas.strokeRect(rectangle.removeClip(), color, width)
    }

    override fun fillPolygon(points: List<Vector>, color: Color) {
        canvas.fillPolygon(points.map { it.removeClip() }, color)
    }

    override fun strokePolygon(points: List<Vector>, color: Color, width: Double) {
        canvas.strokePolygon(points.map { it.removeClip() }, color, width)
    }

    override fun strokeLine(points: List<Vector>, color: Color, width: Double) {
        canvas.strokeLine(points.map { it.removeClip() }, color, width)
    }

    override fun dashLine(points: List<Vector>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        canvas.dashLine(points.map { it.removeClip() }, color, width, dashes, dashOffset)
    }

    override fun fillText(
        text: String,
        position: Vector,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        canvas.fillText(text, position.removeClip(), color, fontSize, alignment, fontWeight)
    }

    override fun fillArc(center: Vector, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas.fillArc(center.removeClip(), radius, startAngle, extendAngle, color)
    }

    override fun strokeArc(
        center: Vector,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
        canvas.strokeArc(center.removeClip(), radius, startAngle, extendAngle, color, width)
    }

    override fun openContextMenu(menu: ContextMenu) {
        canvas.openContextMenu(menu.copy(
            position = menu.position.removeClip()
        ))
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Rectangle.removeClip(): Rectangle {
        return Rectangle(
            left + clip.left,
            top + clip.top,
            width,
            height
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Vector.removeClip(): Vector {
        return this + clip.topLeft
    }
}
