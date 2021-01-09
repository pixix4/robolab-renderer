package de.robolab.client.ui.adapter

import de.robolab.client.renderer.canvas.CanvasListenerManager
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.renderer.events.*
import de.robolab.client.ui.views.ContextMenuView
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.electron
import de.robolab.client.utils.noElectron
import de.robolab.common.utils.*
import de.westermann.kwebview.Document
import de.westermann.kwebview.components.Canvas
import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.math.PI

class WebCanvas(val canvas: Canvas) : ICanvas {

    private val context = canvas.context

    private val hammer = Hammer(canvas.html, js("{}"))

    private var lastPoint: Point? = null

    private fun mouseEventToPointerEvent(event: MouseEvent): PointerEvent {
        return PointerEvent(
            pointFromEvent(event),
            dimension,
            event.ctrlKey,
            event.altKey,
            event.shiftKey
        )
    }

    private fun hammerEventToPointerEvent(event: HammerEvent): PointerEvent {
        return PointerEvent(
            pointFromEvent(event),
            dimension,
            event.ctrlKey,
            event.altKey,
            event.shiftKey
        )
    }

    private fun pointFromEvent(event: MouseEvent): Point {
        val point = Point(event.clientX - canvas.offsetLeftTotal, event.clientY - canvas.offsetTopTotal)
        lastPoint = point
        return point
    }

    private fun pointFromEvent(event: HammerEvent): Point {
        val point = Point(event.center.x - canvas.offsetLeftTotal, event.center.y - canvas.offsetTopTotal)
        lastPoint = point
        return point
    }

    private fun pointFromEvent(event: GestureEvent): Point {
        val point = Point(event.clientX - canvas.offsetLeftTotal, event.clientY - canvas.offsetTopTotal)
        lastPoint = point
        return point
    }

    private val listenerManager = CanvasListenerManager()
    override fun addListener(listener: ICanvasListener) {
        listenerManager += listener
    }

    override fun removeListener(listener: ICanvasListener) {
        listenerManager -= listener
    }

    override val dimension: Dimension
        get() = Dimension(canvas.fixedWidth.toDouble(), canvas.fixedHeight.toDouble())

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

    override fun fillText(
        text: String,
        position: Point,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
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
        context.font = "$weight${fontSize}px 'Roboto Mono'"

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

    override fun strokeArc(
        center: Point,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
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

    override fun openContextMenu(menu: ContextMenu) {
        val m = menu.copy(position = menu.position + Point(canvas.offsetLeftTotal, canvas.offsetTopTotal))
        electron { electron ->
            electron.menu(m)
        }
        noElectron {
            ContextMenuView.open(m)
        }
    }

    override fun startClip(rectangle: Rectangle) {
        context.save()
        val region = Path2D()
        region.rect(rectangle.left, rectangle.top, rectangle.width, rectangle.height)
        context.clip(region)
    }

    override fun endClip() {
        context.restore()
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

        canvas.onMouseMove { event ->
            event.stopPropagation()
            event.preventDefault()

            listenerManager.onPointerMove(mouseEventToPointerEvent(event))
        }
        canvas.onClick { event ->
            when (event.button) {
                MOUSE_BUTTON_SECONDARY -> {
                    event.stopPropagation()
                    event.preventDefault()
                    listenerManager.onPointerSecondaryAction(mouseEventToPointerEvent(event))
                }
                MOUSE_BUTTON_FORWARD -> {
                    listenerManager.onKeyPress(
                        KeyEvent(
                            KeyCode.REDO,
                            "",
                            event.ctrlKey,
                            event.altKey,
                            event.shiftKey
                        )
                    )
                }
                MOUSE_BUTTON_BACK -> {
                    listenerManager.onKeyPress(
                        KeyEvent(
                            KeyCode.UNDO,
                            "",
                            event.ctrlKey,
                            event.altKey,
                            event.shiftKey
                        )
                    )
                }
            }
        }
        canvas.onContext { event ->
            event.preventDefault()
            event.stopPropagation()
            listenerManager.onPointerSecondaryAction(mouseEventToPointerEvent(event))
        }
        canvas.onMouseEnter { event ->
            event.preventDefault()
            event.stopPropagation()
            listenerManager.onPointerEnter(mouseEventToPointerEvent(event))
        }
        canvas.onMouseLeave { event ->
            event.preventDefault()
            event.stopPropagation()
            listenerManager.onPointerLeave(mouseEventToPointerEvent(event))
            lastPoint = null
        }
        canvas.onWheel { event ->
            event.stopPropagation()
            event.preventDefault()

            val factor = when (event.deltaMode) {
                DOM_DELTA_PIXEL -> 1.5
                DOM_DELTA_LINE -> {
                    window.getComputedStyle(canvas.html).fontSize.replace("px", "").toDoubleOrNull()?.let { it * 1.2 }
                }
                DOM_DELTA_PAGE -> {
                    window.innerHeight.toDouble()
                }
                else -> null
            } ?: 1.0

            listenerManager.onScroll(
                ScrollEvent(
                    pointFromEvent(event),
                    Point(event.deltaX * factor * WHEEL_FACTOR, event.deltaY * factor * WHEEL_FACTOR),
                    dimension,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
                )
            )
        }

        canvas.onKeyDown { event ->
            val code = event.toCommon() ?: return@onKeyDown
            event.stopPropagation()
            event.preventDefault()

            val e = KeyEvent(
                code,
                event.key,
                event.ctrlKey,
                event.altKey,
                event.shiftKey
            )

            listenerManager.onKeyPress(e)

            if (!e.bubbles) {
                event.stopPropagation()
            }
        }
        canvas.onKeyPress { event ->
            val code = event.toCommon() ?: return@onKeyPress
            event.preventDefault()

            val e = KeyEvent(
                code,
                event.key,
                event.ctrlKey,
                event.altKey,
                event.shiftKey
            )

            listenerManager.onKeyPress(e)

            if (!e.bubbles) {
                event.stopPropagation()
            }
        }
        canvas.onKeyUp { event ->
            val code = event.toCommon() ?: return@onKeyUp
            event.preventDefault()

            val e = KeyEvent(
                code,
                event.key,
                event.ctrlKey,
                event.altKey,
                event.shiftKey
            )

            listenerManager.onKeyRelease(e)

            if (!e.bubbles) {
                event.stopPropagation()
            }
        }

        hammer.onPanStart { event ->
            event.preventDefault()

            listenerManager.onPointerDown(hammerEventToPointerEvent(event))
        }
        hammer.onPanMove { event ->
            event.preventDefault()

            listenerManager.onPointerDrag(hammerEventToPointerEvent(event))
        }
        hammer.onPanEnd { event ->
            event.preventDefault()

            listenerManager.onPointerUp(hammerEventToPointerEvent(event))
        }

        hammer.onTap { event ->
            event.preventDefault()

            if (event.tapCount > 1 && event.isTouch()) {
                listenerManager.onPointerSecondaryAction(hammerEventToPointerEvent(event))
            } else {
                listenerManager.onPointerDown(hammerEventToPointerEvent(event))
                listenerManager.onPointerUp(hammerEventToPointerEvent(event))
            }
        }

        hammer.onPress { event ->
            event.preventDefault()

            listenerManager.onPointerSecondaryAction(hammerEventToPointerEvent(event))
        }

        var lastScale = 0.0
        hammer.onPinchStart { event ->
            event.preventDefault()

            lastScale = event.scale
        }
        hammer.onPinchMove { event ->
            event.preventDefault()

            val delta = 1.0 - (lastScale - event.scale)
            listenerManager.onZoom(
                ZoomEvent(
                    pointFromEvent(event),
                    delta,
                    dimension,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
                )
            )
            lastScale = event.scale
        }
        hammer.onPinchEnd { event ->
            event.preventDefault()
        }

        var lastRotation = 0.0
        hammer.onRotateStart { event ->
            event.preventDefault()

            lastRotation = event.rotation
        }
        hammer.onRotateMove { event ->
            event.preventDefault()

            val delta = (event.rotation - lastRotation) / 180.0 * PI
            listenerManager.onRotate(
                RotateEvent(
                    pointFromEvent(event),
                    delta,
                    dimension,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
                )
            )
            lastRotation = event.rotation
        }
        hammer.onRotateEnd { event ->
            event.preventDefault()
        }

        if (js("!window.TouchEvent") == true) {
            var lastGestureRotate = 0.0
            var lastGestureScale = 1.0
            Document.onGestureStart { event ->
                event.preventDefault()

                lastGestureRotate = event.rotation
                lastGestureScale = event.scale
            }
            Document.onGestureChange { event ->
                event.preventDefault()

                val deltaRotate = (event.rotation - lastGestureRotate) / 180.0 * PI
                val deltaScale = 1.0 - (lastGestureScale - event.scale) / 4

                lastGestureRotate = event.rotation
                lastGestureScale = event.scale

                if (deltaScale != 1.0) {
                    listenerManager.onZoom(
                        ZoomEvent(
                            pointFromEvent(event),
                            deltaScale,
                            dimension,
                            event.ctrlKey,
                            event.altKey,
                            event.shiftKey
                        )
                    )
                }

                if (deltaRotate != 0.0) {
                    listenerManager.onRotate(
                        RotateEvent(
                            pointFromEvent(event),
                            deltaRotate,
                            dimension,
                            event.ctrlKey,
                            event.altKey,
                            event.shiftKey
                        )
                    )
                }
            }
            Document.onGestureEnd { event ->
                event.preventDefault()
            }
        }

        electron { electron ->
            electron.ipcRenderer.on("rotate-gesture") { _, args ->
                val rotation = -args.rotation.unsafeCast<Double>() / 180.0 * PI
                val point = lastPoint
                if (point != null && rotation != 0.0) {
                    listenerManager.onRotate(
                        RotateEvent(
                            point,
                            rotation,
                            dimension,
                            ctrlKey = false,
                            altKey = false,
                            shiftKey = false
                        )
                    )
                }
            }
        }

        canvas.onResize {
            listenerManager.onResize(dimension)
        }
    }

    companion object {
        const val MOUSE_BUTTON_SECONDARY: Short = 2
        const val MOUSE_BUTTON_BACK: Short = 3
        const val MOUSE_BUTTON_FORWARD: Short = 4

        const val WHEEL_FACTOR = -0.5
        const val DOM_DELTA_PIXEL = 0
        const val DOM_DELTA_LINE = 1
        const val DOM_DELTA_PAGE = 2
    }
}

fun KeyboardEvent.toCommon() = when (key.toLowerCase()) {
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
        Logger("KeyMapper").info { "Unsupported keyCode: $key" }
        null
    }
}
