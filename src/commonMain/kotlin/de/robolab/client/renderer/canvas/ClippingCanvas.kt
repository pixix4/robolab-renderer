package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.events.*
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.*

class ClippingCanvas(private val canvas: ICanvas, initClip: Rectangle) : ICanvas by canvas {

    var clip = initClip
        set(value) {
            field = value
            iCanvasListener?.onResize(value.dimension)
        }

    private var iCanvasListener: ICanvasListener? = null
    override fun setListener(listener: ICanvasListener) {
        this.iCanvasListener = listener
    }

    val transformListener = object : ICanvasListener {
        override fun onPointerDown(event: PointerEvent) {
            iCanvasListener?.onPointerDown(event.clip(clip))
        }

        override fun onPointerUp(event: PointerEvent) {
            iCanvasListener?.onPointerUp(event.clip(clip))
        }

        override fun onPointerMove(event: PointerEvent) {
            iCanvasListener?.onPointerMove(event.clip(clip))
        }

        override fun onPointerDrag(event: PointerEvent) {
            iCanvasListener?.onPointerDrag(event.clip(clip))
        }

        override fun onPointerSecondaryAction(event: PointerEvent) {
            iCanvasListener?.onPointerSecondaryAction(event.clip(clip))
        }

        override fun onPointerEnter(event: PointerEvent) {
            iCanvasListener?.onPointerEnter(event.clip(clip))
        }

        override fun onPointerLeave(event: PointerEvent) {
            iCanvasListener?.onPointerLeave(event.clip(clip))
        }

        override fun onScroll(event: ScrollEvent) {
            iCanvasListener?.onScroll(event.clip(clip))
        }

        override fun onZoom(event: ZoomEvent) {
            iCanvasListener?.onZoom(event.clip(clip))
        }

        override fun onRotate(event: RotateEvent) {
            iCanvasListener?.onRotate(event.clip(clip))
        }

        override fun onKeyPress(event: KeyEvent) {
            iCanvasListener?.onKeyPress(event)
        }

        override fun onKeyRelease(event: KeyEvent) {
            iCanvasListener?.onKeyRelease(event)
        }

        override fun onResize(size: Dimension) {
            iCanvasListener?.onResize(clip.dimension)
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

    override fun fillPolygon(points: List<Point>, color: Color) {
        canvas.fillPolygon(points.map { it.removeClip() }, color)
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        canvas.strokePolygon(points.map { it.removeClip() }, color, width)
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        canvas.strokeLine(points.map { it.removeClip() }, color, width)
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        canvas.dashLine(points.map { it.removeClip() }, color, width, dashes, dashOffset)
    }

    override fun fillText(
        text: String,
        position: Point,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        canvas.fillText(text, position.removeClip(), color, fontSize, alignment, fontWeight)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas.fillArc(center.removeClip(), radius, startAngle, extendAngle, color)
    }

    override fun strokeArc(
        center: Point,
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
    private inline fun Point.removeClip(): Point {
        return this + clip.topLeft
    }
}
