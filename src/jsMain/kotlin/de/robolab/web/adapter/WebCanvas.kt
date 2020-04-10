package de.robolab.web.adapter

import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.*
import de.westermann.kwebview.components.Canvas
import org.w3c.dom.*
import org.w3c.dom.events.MouseEvent
import kotlin.math.PI

class WebCanvas(val canvas: Canvas) : ICanvas {

    private val context = canvas.context
    private val hammer = Hammer(canvas.html, object {})
    private val isTouchSupported = js("!!window.TouchEvent") == true


    private fun shouldIgnoreHammerEvent(event: HammerEvent): Boolean {
        return !(isTouchSupported && event.isTouch() || !isTouchSupported && event.isMouse())
    }

    private fun mouseEventToPointerEvent(event: MouseEvent): PointerEvent {
        return PointerEvent(
                Point(event.clientX - canvas.offsetLeftTotal, event.clientY - canvas.offsetTopTotal),
                Dimension(width, height),
                event.ctrlKey,
                event.altKey,
                event.shiftKey
        )
    }

    private fun hammerEventToPointerEvent(event: HammerEvent): PointerEvent {
        var ctrlKey = false
        var altKey = false
        var shiftKey = false

        val mouseEvent = event.srcEvent as? MouseEvent
        if (mouseEvent != null) {
            ctrlKey = mouseEvent.ctrlKey
            altKey = mouseEvent.altKey
            shiftKey = mouseEvent.shiftKey
        }
        if (isTouchSupported) {
            val touchEvent = event.srcEvent as? TouchEvent
            if (touchEvent != null) {
                ctrlKey = touchEvent.ctrlKey
                altKey = touchEvent.altKey
                shiftKey = touchEvent.shiftKey
            }
        }

        return PointerEvent(
                Point(event.center.x - canvas.offsetLeftTotal, event.center.y - canvas.offsetTopTotal),
                Dimension(width, height),
                ctrlKey,
                altKey,
                shiftKey
        )
    }

    override fun setListener(listener: ICanvasListener) {
        canvas.onMouseMove { event ->
            event.stopPropagation()
            event.preventDefault()

            listener.onPointerMove(mouseEventToPointerEvent(event))
        }
        canvas.onClick { event ->
            when (event.button) {
                MOUSE_BUTTON_SECONDARY -> {
                    event.stopPropagation()
                    event.preventDefault()
                    listener.onPointerSecondaryAction(mouseEventToPointerEvent(event))
                }
                MOUSE_BUTTON_FORWARD -> {
                    listener.onKeyPress(KeyEvent(
                            KeyCode.REDO,
                            "",
                            event.ctrlKey,
                            event.altKey,
                            event.shiftKey
                    ))
                }
                MOUSE_BUTTON_BACK -> {
                    listener.onKeyPress(KeyEvent(
                            KeyCode.UNDO,
                            "",
                            event.ctrlKey,
                            event.altKey,
                            event.shiftKey
                    ))
                }
            }
        }
        canvas.onContext { event ->
            event.preventDefault()
            event.stopPropagation()
            listener.onPointerSecondaryAction(mouseEventToPointerEvent(event))
        }
        canvas.onMouseEnter { event ->
            event.preventDefault()
            event.stopPropagation()
            listener.onPointerEnter(mouseEventToPointerEvent(event))
        }
        canvas.onMouseLeave { event ->
            event.preventDefault()
            event.stopPropagation()
            listener.onPointerLeave(mouseEventToPointerEvent(event))
        }
        canvas.onWheel { event ->
            event.stopPropagation()
            event.preventDefault()
            listener.onScroll(ScrollEvent(
                    Point(event.clientX - canvas.offsetLeftTotal, event.clientY - canvas.offsetTopTotal),
                    Point(event.deltaX * WHEEL_FACTOR, event.deltaY * WHEEL_FACTOR),
                    Dimension(width, height),
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }

        canvas.onKeyDown { event ->
            val code = event.key.toCommon() ?: return@onKeyDown
            event.stopPropagation()
            event.preventDefault()
            listener.onKeyPress(KeyEvent(
                    code,
                    event.key,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }
        canvas.onKeyPress { event ->
            val code = event.key.toCommon() ?: return@onKeyPress
            event.stopPropagation()
            event.preventDefault()
            listener.onKeyPress(KeyEvent(
                    code,
                    event.key,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }
        canvas.onKeyUp { event ->
            val code = event.key.toCommon() ?: return@onKeyUp
            event.stopPropagation()
            event.preventDefault()
            listener.onKeyRelease(KeyEvent(
                    code,
                    event.key,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }

        hammer.onPanStart { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onPanStart

            listener.onPointerDown(hammerEventToPointerEvent(event))
        }
        hammer.onPanMove { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onPanMove

            listener.onPointerDrag(hammerEventToPointerEvent(event))
        }
        hammer.onPanEnd { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onPanEnd

            listener.onPointerUp(hammerEventToPointerEvent(event))
        }

        hammer.onTap { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onTap

            if (event.tapCount > 1 && event.isTouch()) {
                listener.onPointerSecondaryAction(hammerEventToPointerEvent(event))
            } else {
                listener.onPointerDown(hammerEventToPointerEvent(event))
                listener.onPointerUp(hammerEventToPointerEvent(event))
            }
        }

        hammer.onPress { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onPress

            listener.onPointerSecondaryAction(hammerEventToPointerEvent(event))
        }

        var lastScale = 0.0
        hammer.onPinchStart { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onPinchStart

            lastScale = event.scale
        }
        hammer.onPinchMove { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onPinchMove

            val delta = 1.0 - (lastScale - event.scale)
            listener.onZoom(ZoomEvent(
                    Point(event.center.x - canvas.offsetLeftTotal, event.center.y - canvas.offsetTopTotal),
                    delta,
                    Dimension(width, height),
                    ctrlKey = false,
                    altKey = false,
                    shiftKey = false
            ))
            lastScale = event.scale
        }
        hammer.onPinchEnd { event ->
            event.preventDefault()
        }

        var lastRotation = 0.0
        hammer.onRotateStart { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onRotateStart

            lastRotation = event.rotation
        }
        hammer.onRotateMove { event ->
            event.preventDefault()
            if (shouldIgnoreHammerEvent(event)) return@onRotateMove

            val delta = (event.rotation - lastRotation) / 180.0 * PI
            listener.onRotate(RotateEvent(
                    Point(event.center.x - canvas.offsetLeftTotal, event.center.y - canvas.offsetTopTotal),
                    delta,
                    Dimension(width, height),
                    ctrlKey = false,
                    altKey = false,
                    shiftKey = false
            ))
            lastRotation = event.rotation
        }
        hammer.onRotateEnd { event ->
            event.preventDefault()
        }

        canvas.onResize {
            listener.onResize(Dimension(width, height))
        }
    }

    override val width: Double
        get() = canvas.fixedWidth.toDouble()

    override val height: Double
        get() = canvas.fixedHeight.toDouble()

    override fun clear(color: Color) {
        fillRect(Rectangle(
                0.0,
                0.0,
                width,
                height
        ), color)
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.fillStyle = color.toString()

        context.fillRect(
                rectangle.left,
                rectangle.top,
                rectangle.width,
                rectangle.height
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        context.strokeRect(
                rectangle.left,
                rectangle.top,
                rectangle.width,
                rectangle.height
        )
    }

    private fun drawPath(points: List<Point>) {
        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.fillStyle = color.toString()

        drawPath(points)
        context.closePath()

        context.fill()
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        drawPath(points)
        context.closePath()

        context.stroke()
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        drawPath(points)

        context.stroke()
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width
        context.setLineDash(dashes.toTypedArray())
        context.lineDashOffset = dashOffset

        drawPath(points)

        context.stroke()

        context.setLineDash(arrayOf())
        context.lineDashOffset = 0.0
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double, alignment: ICanvas.FontAlignment, fontWeight: ICanvas.FontWeight) {
        context.fillStyle = color.toString()
        context.textAlign = when (alignment) {
            ICanvas.FontAlignment.LEFT -> CanvasTextAlign.LEFT
            ICanvas.FontAlignment.CENTER -> CanvasTextAlign.CENTER
            ICanvas.FontAlignment.RIGHT -> CanvasTextAlign.RIGHT
        }
        context.textBaseline = CanvasTextBaseline.MIDDLE
        val weight = when (fontWeight) {
            ICanvas.FontWeight.NORMAL -> ""
            ICanvas.FontWeight.BOLD -> "bold "
        }
        context.font = "$weight${fontSize}px sans-serif"

        context.fillText(text, position.left, position.top)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.fillStyle = color.toString()

        context.beginPath()

        context.arc(
                center.left,
                center.top,
                radius,
                2.0 * PI - startAngle,
                2.0 * PI - (startAngle + extendAngle),
                anticlockwise = true
        )

        context.fill()
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        context.beginPath()

        context.arc(
                center.left,
                center.top,
                radius,
                2.0 * PI - startAngle,
                2.0 * PI - (startAngle + extendAngle),
                anticlockwise = true
        )

        context.stroke()
    }

    init {
        context.lineCap = CanvasLineCap.BUTT
        context.lineJoin = CanvasLineJoin.MITER

        hammer.enablePan()
        hammer.enablePinch()
        hammer.enableRotate()
        hammer.enablePress()
        hammer.enableTap()

        canvas.html.tabIndex = 0
    }

    companion object {
        const val MOUSE_BUTTON_SECONDARY: Short = 2
        const val MOUSE_BUTTON_BACK: Short = 3
        const val MOUSE_BUTTON_FORWARD: Short = 4

        const val WHEEL_FACTOR = -4.0
    }
}

private fun String.toCommon() = when (this.toLowerCase()) {
    "," -> KeyCode.COMMA
    "<" -> KeyCode.ANGLE_BRACKET_LEFT
    "." -> KeyCode.PERIOD
    ">" -> KeyCode.ANGLE_BRACKET_RIGHT
    "/" -> KeyCode.SLASH
    "?" -> KeyCode.QUESTION_MARK
    ";" -> KeyCode.SEMICOLON
    ":" -> KeyCode.COLON
    "'" -> KeyCode.QUOTE
    "\"" -> KeyCode.DOUBLE_QUOTE
    "\\" -> KeyCode.BACKSLASH
    "|" -> KeyCode.PIPE
    "[" -> KeyCode.SQUARE_BRACKET_LEFT
    "{" -> KeyCode.CURLY_BRACKET_LEFT
    "]" -> KeyCode.SQUARE_BRACKET_RIGHT
    "}" -> KeyCode.CURLY_BRACKET_RIGHT
    "!" -> KeyCode.EXCLAMATION_MARK
    "@" -> KeyCode.AT
    "#" -> KeyCode.HASH
    "$" -> KeyCode.DOLLAR
    "€" -> KeyCode.EURO
    "%" -> KeyCode.PERCENT
    "&" -> KeyCode.AND
    "*" -> KeyCode.MULTIPLY
    "(" -> KeyCode.ROUND_BRACKET_LEFT
    ")" -> KeyCode.ROUND_BRACKET_RIGHT
    "-" -> KeyCode.MINUS
    "_" -> KeyCode.UNDERSCORE
    "=" -> KeyCode.EQUALS
    "+" -> KeyCode.PLUS
    "1" -> KeyCode.NUM_1
    "2" -> KeyCode.NUM_2
    "3" -> KeyCode.NUM_3
    "4" -> KeyCode.NUM_4
    "5" -> KeyCode.NUM_5
    "6" -> KeyCode.NUM_6
    "7" -> KeyCode.NUM_7
    "8" -> KeyCode.NUM_8
    "9" -> KeyCode.NUM_9
    "0" -> KeyCode.NUM_0
    "a" -> KeyCode.A
    "b" -> KeyCode.B
    "c" -> KeyCode.C
    "d" -> KeyCode.D
    "e" -> KeyCode.E
    "f" -> KeyCode.F
    "g" -> KeyCode.G
    "h" -> KeyCode.H
    "i" -> KeyCode.I
    "j" -> KeyCode.J
    "k" -> KeyCode.K
    "l" -> KeyCode.L
    "m" -> KeyCode.M
    "n" -> KeyCode.N
    "o" -> KeyCode.O
    "p" -> KeyCode.P
    "q" -> KeyCode.Q
    "r" -> KeyCode.R
    "s" -> KeyCode.S
    "t" -> KeyCode.T
    "u" -> KeyCode.U
    "v" -> KeyCode.V
    "w" -> KeyCode.W
    "x" -> KeyCode.X
    "y" -> KeyCode.Y
    "z" -> KeyCode.Z
    " " -> KeyCode.SPACE
    "tab" -> KeyCode.TAB
    "shift" -> KeyCode.SHIFT
    "control" -> KeyCode.CTRL
    "alt" -> KeyCode.ALT
    "altgraph" -> KeyCode.ALT_GRAPHICS
    "print" -> KeyCode.PRINT
    "escape" -> KeyCode.ESCAPE
    "home" -> KeyCode.HOME
    "end" -> KeyCode.END
    "insert" -> KeyCode.INSERT
    "delete" -> KeyCode.DELETE
    "backspace" -> KeyCode.BACKSPACE
    "enter" -> KeyCode.ENTER
    "pageup" -> KeyCode.PAGE_UP
    "pagedown" -> KeyCode.PAGE_DOWN
    "arrowup" -> KeyCode.ARROW_UP
    "arrowleft" -> KeyCode.ARROW_LEFT
    "arrowdown" -> KeyCode.ARROW_DOWN
    "arrowright" -> KeyCode.ARROW_RIGHT
    "undo" -> KeyCode.UNDO
    "redo" -> KeyCode.REDO
    "cut" -> KeyCode.CUT
    "copy" -> KeyCode.COPY
    "paste" -> KeyCode.PASTE
    "find" -> KeyCode.FIND
    // "f1" -> KeyCode.F1
    // "f2" -> KeyCode.F2
    // "f3" -> KeyCode.F3
    // "f4" -> KeyCode.F4
    // "f5" -> KeyCode.F5
    // "f6" -> KeyCode.F6
    // "f7" -> KeyCode.F7
    // "f8" -> KeyCode.F8
    // "f9" -> KeyCode.F9
    // "f10" -> KeyCode.F10
    // "f11" -> KeyCode.F11
    // "f12" -> KeyCode.F12
    else -> {
        println("Unsupported keyCode: $this")
        null
    }
}
