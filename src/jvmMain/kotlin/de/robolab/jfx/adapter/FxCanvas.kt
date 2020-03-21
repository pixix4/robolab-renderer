package de.robolab.jfx.adapter

import de.robolab.jfx.ResizeableCanvas
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.*
import javafx.geometry.VPos
import javafx.scene.input.MouseButton
import javafx.scene.shape.ArcType
import javafx.scene.shape.StrokeLineCap
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import kotlin.math.PI

class FxCanvas : ICanvas {

    val canvas = ResizeableCanvas()
    private val context = canvas.graphicsContext2D

    override fun setListener(listener: ICanvasListener) {
        canvas.setOnMousePressed { event ->
            canvas.requestFocus()
            when (event.button) {
                MouseButton.FORWARD -> listener.onKeyDown(KeyEvent(
                        KeyCode.AGAIN,
                        "",
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                ))
                MouseButton.BACK -> listener.onKeyDown(KeyEvent(
                        KeyCode.UNDO,
                        "",
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                ))
                else -> listener.onMouseDown(MouseEvent(
                        Point(event.x, event.y),
                        Dimension(width, height),
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                ))
            }

        }
        canvas.setOnMouseReleased { event ->
            when (event.button) {
                MouseButton.FORWARD -> listener.onKeyUp(KeyEvent(
                        KeyCode.AGAIN,
                        "",
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                ))
                MouseButton.BACK -> listener.onKeyUp(KeyEvent(
                        KeyCode.UNDO,
                        "",
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                ))
                else -> listener.onMouseUp(MouseEvent(
                        Point(event.x, event.y),
                        Dimension(width, height),
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                ))
            }
        }
        canvas.setOnMouseDragged { event ->
            listener.onMouseDrag(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnMouseMoved { event ->
            listener.onMouseMove(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnMouseClicked { event ->
            listener.onMouseClick(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnScroll { event ->
            listener.onScroll(ScrollEvent(
                    Point(event.x, event.y),
                    Point(event.deltaX, event.deltaY),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnZoom { event ->
            listener.onZoom(ZoomEvent(
                    Point(event.x, event.y),
                    event.zoomFactor,
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnRotate { event ->
            listener.onRotate(RotateEvent(
                    Point(event.x, event.y),
                    event.angle / 180.0 * PI,
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.addDrawHook {
            listener.onResize(Dimension(width, height))
        }
        canvas.setOnKeyPressed { event ->
            val code = event.code.toCommon() ?: return@setOnKeyPressed
            listener.onKeyDown(KeyEvent(
                    code,
                    event.text,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnKeyReleased { event ->
            val code = event.code.toCommon() ?: return@setOnKeyReleased
            listener.onKeyUp(KeyEvent(
                    code,
                    event.text,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
    }

    override val width: Double
        get() = canvas.width

    override val height: Double
        get() = canvas.height

    override fun clear(color: Color) {
        fillRect(Rectangle(
                0.0,
                0.0,
                width,
                height
        ), color)
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.fill = color.fx()

        context.fillRect(
                rectangle.left,
                rectangle.top,
                rectangle.width,
                rectangle.height
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.strokeRect(
                rectangle.left,
                rectangle.top,
                rectangle.width,
                rectangle.height
        )
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.fill = color.fx()

        context.fillPolygon(
                points.map { it.left }.toDoubleArray(),
                points.map { it.top }.toDoubleArray(),
                points.size
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.strokePolygon(
                points.map { it.left }.toDoubleArray(),
                points.map { it.top }.toDoubleArray(),
                points.size
        )
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.stroke = color.fx()
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
        context.fill = color.fx()
        context.textAlign = TextAlignment.CENTER
        context.textBaseline = VPos.CENTER
        context.font = Font.font(fontSize)

        context.fillText(text, position.left, position.top)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.fill = color.fx()

        context.fillArc(
                center.left - radius,
                center.top - radius,
                radius * 2,
                radius * 2,
                startAngle / PI * 180.0,
                extendAngle / PI * 180.0,
                ArcType.CHORD
        )
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.strokeArc(
                center.left - radius,
                center.top - radius,
                radius * 2,
                radius * 2,
                startAngle / PI * 180.0,
                extendAngle / PI * 180.0,
                ArcType.OPEN
        )
    }

    init {
        context.lineCap = StrokeLineCap.BUTT
        canvas.isFocusTraversable = true
    }
}

fun Color.fx(): javafx.scene.paint.Color = javafx.scene.paint.Color.rgb(red, green, blue, alpha)

fun javafx.scene.input.KeyCode.toCommon() = when (this) {
    javafx.scene.input.KeyCode.ENTER -> KeyCode.ENTER
    javafx.scene.input.KeyCode.BACK_SPACE -> KeyCode.BACK_SPACE
    javafx.scene.input.KeyCode.TAB -> KeyCode.TAB
    javafx.scene.input.KeyCode.CANCEL -> KeyCode.CANCEL
    javafx.scene.input.KeyCode.CLEAR -> KeyCode.CLEAR
    javafx.scene.input.KeyCode.SHIFT -> KeyCode.SHIFT
    javafx.scene.input.KeyCode.CONTROL -> KeyCode.CONTROL
    javafx.scene.input.KeyCode.ALT -> KeyCode.ALT
    javafx.scene.input.KeyCode.ESCAPE -> KeyCode.ESCAPE
    javafx.scene.input.KeyCode.SPACE -> KeyCode.SPACE
    javafx.scene.input.KeyCode.PAGE_UP -> KeyCode.PAGE_UP
    javafx.scene.input.KeyCode.PAGE_DOWN -> KeyCode.PAGE_DOWN
    javafx.scene.input.KeyCode.END -> KeyCode.END
    javafx.scene.input.KeyCode.HOME -> KeyCode.HOME
    javafx.scene.input.KeyCode.LEFT -> KeyCode.LEFT
    javafx.scene.input.KeyCode.UP -> KeyCode.UP
    javafx.scene.input.KeyCode.RIGHT -> KeyCode.RIGHT
    javafx.scene.input.KeyCode.DOWN -> KeyCode.DOWN
    javafx.scene.input.KeyCode.COMMA -> KeyCode.COMMA
    javafx.scene.input.KeyCode.MINUS -> KeyCode.MINUS
    javafx.scene.input.KeyCode.PERIOD -> KeyCode.PERIOD
    javafx.scene.input.KeyCode.SLASH -> KeyCode.SLASH
    javafx.scene.input.KeyCode.DIGIT0 -> KeyCode.DIGIT0
    javafx.scene.input.KeyCode.DIGIT1 -> KeyCode.DIGIT1
    javafx.scene.input.KeyCode.DIGIT2 -> KeyCode.DIGIT2
    javafx.scene.input.KeyCode.DIGIT3 -> KeyCode.DIGIT3
    javafx.scene.input.KeyCode.DIGIT4 -> KeyCode.DIGIT4
    javafx.scene.input.KeyCode.DIGIT5 -> KeyCode.DIGIT5
    javafx.scene.input.KeyCode.DIGIT6 -> KeyCode.DIGIT6
    javafx.scene.input.KeyCode.DIGIT7 -> KeyCode.DIGIT7
    javafx.scene.input.KeyCode.DIGIT8 -> KeyCode.DIGIT8
    javafx.scene.input.KeyCode.DIGIT9 -> KeyCode.DIGIT9
    javafx.scene.input.KeyCode.SEMICOLON -> KeyCode.SEMICOLON
    javafx.scene.input.KeyCode.EQUALS -> KeyCode.EQUALS
    javafx.scene.input.KeyCode.A -> KeyCode.A
    javafx.scene.input.KeyCode.B -> KeyCode.B
    javafx.scene.input.KeyCode.C -> KeyCode.C
    javafx.scene.input.KeyCode.D -> KeyCode.D
    javafx.scene.input.KeyCode.E -> KeyCode.E
    javafx.scene.input.KeyCode.F -> KeyCode.F
    javafx.scene.input.KeyCode.G -> KeyCode.G
    javafx.scene.input.KeyCode.H -> KeyCode.H
    javafx.scene.input.KeyCode.I -> KeyCode.I
    javafx.scene.input.KeyCode.J -> KeyCode.J
    javafx.scene.input.KeyCode.K -> KeyCode.K
    javafx.scene.input.KeyCode.L -> KeyCode.L
    javafx.scene.input.KeyCode.M -> KeyCode.M
    javafx.scene.input.KeyCode.N -> KeyCode.N
    javafx.scene.input.KeyCode.O -> KeyCode.O
    javafx.scene.input.KeyCode.P -> KeyCode.P
    javafx.scene.input.KeyCode.Q -> KeyCode.Q
    javafx.scene.input.KeyCode.R -> KeyCode.R
    javafx.scene.input.KeyCode.S -> KeyCode.S
    javafx.scene.input.KeyCode.T -> KeyCode.T
    javafx.scene.input.KeyCode.U -> KeyCode.U
    javafx.scene.input.KeyCode.V -> KeyCode.V
    javafx.scene.input.KeyCode.W -> KeyCode.W
    javafx.scene.input.KeyCode.X -> KeyCode.X
    javafx.scene.input.KeyCode.Y -> KeyCode.Y
    javafx.scene.input.KeyCode.Z -> KeyCode.Z
    javafx.scene.input.KeyCode.OPEN_BRACKET -> KeyCode.OPEN_BRACKET
    javafx.scene.input.KeyCode.BACK_SLASH -> KeyCode.BACK_SLASH
    javafx.scene.input.KeyCode.CLOSE_BRACKET -> KeyCode.CLOSE_BRACKET
    javafx.scene.input.KeyCode.NUMPAD0 -> KeyCode.NUMPAD0
    javafx.scene.input.KeyCode.NUMPAD1 -> KeyCode.NUMPAD1
    javafx.scene.input.KeyCode.NUMPAD2 -> KeyCode.NUMPAD2
    javafx.scene.input.KeyCode.NUMPAD3 -> KeyCode.NUMPAD3
    javafx.scene.input.KeyCode.NUMPAD4 -> KeyCode.NUMPAD4
    javafx.scene.input.KeyCode.NUMPAD5 -> KeyCode.NUMPAD5
    javafx.scene.input.KeyCode.NUMPAD6 -> KeyCode.NUMPAD6
    javafx.scene.input.KeyCode.NUMPAD7 -> KeyCode.NUMPAD7
    javafx.scene.input.KeyCode.NUMPAD8 -> KeyCode.NUMPAD8
    javafx.scene.input.KeyCode.NUMPAD9 -> KeyCode.NUMPAD9
    javafx.scene.input.KeyCode.MULTIPLY -> KeyCode.MULTIPLY
    javafx.scene.input.KeyCode.ADD -> KeyCode.ADD
    javafx.scene.input.KeyCode.SEPARATOR -> KeyCode.SEPARATOR
    javafx.scene.input.KeyCode.SUBTRACT -> KeyCode.SUBTRACT
    javafx.scene.input.KeyCode.DECIMAL -> KeyCode.DECIMAL
    javafx.scene.input.KeyCode.DIVIDE -> KeyCode.DIVIDE
    javafx.scene.input.KeyCode.DELETE -> KeyCode.DELETE
    javafx.scene.input.KeyCode.F1 -> KeyCode.F1
    javafx.scene.input.KeyCode.F2 -> KeyCode.F2
    javafx.scene.input.KeyCode.F3 -> KeyCode.F3
    javafx.scene.input.KeyCode.F4 -> KeyCode.F4
    javafx.scene.input.KeyCode.F5 -> KeyCode.F5
    javafx.scene.input.KeyCode.F6 -> KeyCode.F6
    javafx.scene.input.KeyCode.F7 -> KeyCode.F7
    javafx.scene.input.KeyCode.F8 -> KeyCode.F8
    javafx.scene.input.KeyCode.F9 -> KeyCode.F9
    javafx.scene.input.KeyCode.F10 -> KeyCode.F10
    javafx.scene.input.KeyCode.F11 -> KeyCode.F11
    javafx.scene.input.KeyCode.F12 -> KeyCode.F12
    javafx.scene.input.KeyCode.F13 -> KeyCode.F13
    javafx.scene.input.KeyCode.F14 -> KeyCode.F14
    javafx.scene.input.KeyCode.F15 -> KeyCode.F15
    javafx.scene.input.KeyCode.F16 -> KeyCode.F16
    javafx.scene.input.KeyCode.F17 -> KeyCode.F17
    javafx.scene.input.KeyCode.F18 -> KeyCode.F18
    javafx.scene.input.KeyCode.F19 -> KeyCode.F19
    javafx.scene.input.KeyCode.F20 -> KeyCode.F20
    javafx.scene.input.KeyCode.F21 -> KeyCode.F21
    javafx.scene.input.KeyCode.F22 -> KeyCode.F22
    javafx.scene.input.KeyCode.F23 -> KeyCode.F23
    javafx.scene.input.KeyCode.F24 -> KeyCode.F24
    javafx.scene.input.KeyCode.PRINTSCREEN -> KeyCode.PRINTSCREEN
    javafx.scene.input.KeyCode.INSERT -> KeyCode.INSERT
    javafx.scene.input.KeyCode.HELP -> KeyCode.HELP
    javafx.scene.input.KeyCode.META -> KeyCode.META
    javafx.scene.input.KeyCode.BACK_QUOTE -> KeyCode.BACK_QUOTE
    javafx.scene.input.KeyCode.QUOTE -> KeyCode.QUOTE
    javafx.scene.input.KeyCode.AMPERSAND -> KeyCode.AMPERSAND
    javafx.scene.input.KeyCode.ASTERISK -> KeyCode.ASTERISK
    javafx.scene.input.KeyCode.QUOTEDBL -> KeyCode.QUOTEDBL
    javafx.scene.input.KeyCode.LESS -> KeyCode.LESS
    javafx.scene.input.KeyCode.GREATER -> KeyCode.GREATER
    javafx.scene.input.KeyCode.BRACELEFT -> KeyCode.BRACELEFT
    javafx.scene.input.KeyCode.BRACERIGHT -> KeyCode.BRACERIGHT
    javafx.scene.input.KeyCode.AT -> KeyCode.AT
    javafx.scene.input.KeyCode.COLON -> KeyCode.COLON
    javafx.scene.input.KeyCode.CIRCUMFLEX -> KeyCode.CIRCUMFLEX
    javafx.scene.input.KeyCode.DOLLAR -> KeyCode.DOLLAR
    javafx.scene.input.KeyCode.EURO_SIGN -> KeyCode.EURO_SIGN
    javafx.scene.input.KeyCode.EXCLAMATION_MARK -> KeyCode.EXCLAMATION_MARK
    javafx.scene.input.KeyCode.LEFT_PARENTHESIS -> KeyCode.LEFT_PARENTHESIS
    javafx.scene.input.KeyCode.NUMBER_SIGN -> KeyCode.NUMBER_SIGN
    javafx.scene.input.KeyCode.PLUS -> KeyCode.PLUS
    javafx.scene.input.KeyCode.RIGHT_PARENTHESIS -> KeyCode.RIGHT_PARENTHESIS
    javafx.scene.input.KeyCode.UNDERSCORE -> KeyCode.UNDERSCORE
    javafx.scene.input.KeyCode.WINDOWS -> KeyCode.WINDOWS
    javafx.scene.input.KeyCode.CUT -> KeyCode.CUT
    javafx.scene.input.KeyCode.COPY -> KeyCode.COPY
    javafx.scene.input.KeyCode.PASTE -> KeyCode.PASTE
    javafx.scene.input.KeyCode.UNDO -> KeyCode.UNDO
    javafx.scene.input.KeyCode.AGAIN -> KeyCode.AGAIN
    javafx.scene.input.KeyCode.FIND -> KeyCode.FIND
    else -> {
        println("Unsupported keyCode: ${this.code}")
        null
    }
}