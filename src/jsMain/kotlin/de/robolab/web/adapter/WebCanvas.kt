package de.robolab.jfx.adapter

import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.*
import de.westermann.kwebview.components.Canvas
import de.westermann.kwebview.get
import org.w3c.dom.*
import kotlin.math.PI

class WebCanvas(private val canvas: Canvas) : ICanvas {

    private val context = canvas.context

    override fun setListener(listener: ICanvasListener) {
        canvas.onMouseDown { event ->
            event.stopPropagation()
            event.preventDefault()
            when (event.button) {
                MOUSE_BUTTON_FORWARD -> listener.onKeyDown(KeyEvent(
                        KeyCode.REDO,
                        "",
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
                MOUSE_BUTTON_BACK -> listener.onKeyDown(KeyEvent(
                        KeyCode.UNDO,
                        "",
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
                else -> listener.onMouseDown(MouseEvent(
                        Point(event.x - canvas.offsetLeft, event.y - canvas.offsetTop),
                        Dimension(width, height),
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
            }

        }
        canvas.onMouseUp { event ->
            event.stopPropagation()
            event.preventDefault()
            when (event.button) {
                MOUSE_BUTTON_FORWARD -> listener.onKeyUp(KeyEvent(
                        KeyCode.REDO,
                        "",
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
                MOUSE_BUTTON_BACK -> listener.onKeyUp(KeyEvent(
                        KeyCode.UNDO,
                        "",
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
                else -> listener.onMouseUp(MouseEvent(
                        Point(event.x - canvas.offsetLeft, event.y - canvas.offsetTop),
                        Dimension(width, height),
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
            }
        }
        canvas.onMouseMove { event ->
            event.stopPropagation()
            event.preventDefault()
            if (event.buttons != 0.toShort()) {
                listener.onMouseDrag(MouseEvent(
                        Point(event.x - canvas.offsetLeft, event.y - canvas.offsetTop),
                        Dimension(width, height),
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
            } else {
                listener.onMouseMove(MouseEvent(
                        Point(event.x - canvas.offsetLeft, event.y - canvas.offsetTop),
                        Dimension(width, height),
                        event.ctrlKey,
                        event.altKey,
                        event.shiftKey
                ))
            }
        }
        canvas.onTouchStart { event ->
            event.stopPropagation()
            event.preventDefault()
            val touch = event.changedTouches[0] ?: return@onTouchStart
            listener.onMouseDown(MouseEvent(
                    Point(touch.clientX - canvas.offsetLeft, touch.clientY - canvas.offsetTop),
                    Dimension(width, height),
                    ctrlKey = false,
                    altKey = false,
                    shiftKey = false
            ))
        }
        canvas.onTouchEnd { event ->
            event.stopPropagation()
            event.preventDefault()
            val touch = event.changedTouches[0] ?: return@onTouchEnd
            listener.onMouseUp(MouseEvent(
                    Point(touch.clientX - canvas.offsetLeft, touch.clientY - canvas.offsetTop),
                    Dimension(width, height),
                    ctrlKey = false,
                    altKey = false,
                    shiftKey = false
            ))

        }
        canvas.onTouchCancel { event ->
            event.stopPropagation()
            event.preventDefault()
            val touch = event.changedTouches[0] ?: return@onTouchCancel
            listener.onMouseUp(MouseEvent(
                    Point(touch.clientX - canvas.offsetLeft, touch.clientY - canvas.offsetTop),
                    Dimension(width, height),
                    ctrlKey = false,
                    altKey = false,
                    shiftKey = false
            ))
        }
        canvas.onTouchMove { event ->
            event.stopPropagation()
            event.preventDefault()
            val touch = event.changedTouches[0] ?: return@onTouchMove
            listener.onMouseDrag(MouseEvent(
                    Point(touch.clientX - canvas.offsetLeft, touch.clientY - canvas.offsetTop),
                    Dimension(width, height),
                    ctrlKey = false,
                    altKey = false,
                    shiftKey = false
            ))
        }
        canvas.onClick { event ->
            event.stopPropagation()
            event.preventDefault()
            listener.onMouseClick(MouseEvent(
                    Point(event.x - canvas.offsetLeft, event.y - canvas.offsetTop),
                    Dimension(width, height),
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }
        canvas.onWheel { event ->
            event.stopPropagation()
            event.preventDefault()
            listener.onScroll(ScrollEvent(
                    Point(event.x - canvas.offsetLeft, event.y - canvas.offsetTop),
                    Point(event.deltaX, event.deltaY),
                    Dimension(width, height),
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }
        canvas.onKeyPress { event ->
            val code = event.key.toCommon() ?: return@onKeyPress
            event.stopPropagation()
            event.preventDefault()
            listener.onKeyDown(KeyEvent(
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
            listener.onKeyUp(KeyEvent(
                    code,
                    event.key,
                    event.ctrlKey,
                    event.altKey,
                    event.shiftKey
            ))
        }

        canvas.onResize {
            listener.onResize(Dimension(width, height))
        }
    }

    override val width: Double
        get() = canvas.clientWidth.toDouble()

    override val height: Double
        get() = canvas.clientHeight.toDouble()

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

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.fillStyle = color.toString()

        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }
        context.lineTo(first.left, first.top)

        context.fill()
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }
        context.lineTo(first.left, first.top)

        context.stroke()
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }

        context.stroke()
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double) {
        context.fillStyle = color.toString()
        context.textAlign = CanvasTextAlign.CENTER
        context.textBaseline = CanvasTextBaseline.MIDDLE
        context.font = "${fontSize}px sans-serif"

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
    }

    companion object {
        const val MOUSE_BUTTON_BACK: Short = 3
        const val MOUSE_BUTTON_FORWARD: Short = 4
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
    "f1" -> KeyCode.F1
    "f2" -> KeyCode.F2
    "f3" -> KeyCode.F3
    "f4" -> KeyCode.F4
    "f5" -> KeyCode.F5
    "f6" -> KeyCode.F6
    "f7" -> KeyCode.F7
    "f8" -> KeyCode.F8
    "f9" -> KeyCode.F9
    "f10" -> KeyCode.F10
    "f11" -> KeyCode.F11
    "f12" -> KeyCode.F12
    else -> {
        println("Unsupported keyCode: $this")
        null
    }
}
