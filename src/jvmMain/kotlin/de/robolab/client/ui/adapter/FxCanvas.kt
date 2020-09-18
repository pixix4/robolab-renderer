package de.robolab.client.ui.adapter

import de.robolab.client.renderer.canvas.CanvasListenerManager
import de.robolab.client.ui.view.ResizeableCanvas
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.renderer.events.*
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.*
import javafx.geometry.VPos
import javafx.scene.input.MouseButton
import javafx.scene.shape.ArcType
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import kotlin.math.PI

class FxCanvas : ICanvas {

    val canvas = ResizeableCanvas()
    private val context = canvas.graphicsContext2D
    private val logger = Logger(this)

    private val listenerManager = CanvasListenerManager()
    override fun addListener(listener: ICanvasListener) {
        listenerManager += listener
    }

    override fun removeListener(listener: ICanvasListener) {
        listenerManager -= listener
    }

    override val dimension: Dimension
        get() = Dimension(canvas.width, canvas.height)

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.fill = color.toFx()

        context.fillRect(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            rectangle.height
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.stroke = color.toFx()
        context.lineWidth = width

        context.strokeRect(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            rectangle.height
        )
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.fill = color.toFx()

        context.fillPolygon(
            points.map { it.left }.toDoubleArray(),
            points.map { it.top }.toDoubleArray(),
            points.size
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.stroke = color.toFx()
        context.lineWidth = width

        context.strokePolygon(
            points.map { it.left }.toDoubleArray(),
            points.map { it.top }.toDoubleArray(),
            points.size
        )
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.stroke = color.toFx()
        context.lineWidth = width

        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }

        context.stroke()
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        context.stroke = color.toFx()
        context.lineWidth = width
        context.setLineDashes(*dashes.toDoubleArray())
        context.lineDashOffset = dashOffset

        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }

        context.stroke()

        context.setLineDashes()
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
        context.fill = color.toFx()
        context.textAlign = when (alignment) {
            ICanvas.FontAlignment.LEFT -> TextAlignment.LEFT
            ICanvas.FontAlignment.CENTER -> TextAlignment.CENTER
            ICanvas.FontAlignment.RIGHT -> TextAlignment.RIGHT
        }
        context.textBaseline = VPos.CENTER
        context.font = when (fontWeight) {
            ICanvas.FontWeight.NORMAL -> Font.font("Roboto Mono", fontSize)
            ICanvas.FontWeight.BOLD -> Font.font("Roboto Mono", FontWeight.BOLD, fontSize)
        }

        context.fillText(text, position.left, position.top)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.fill = color.toFx()

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

    override fun strokeArc(
        center: Point,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
        context.stroke = color.toFx()
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

    override fun openContextMenu(menu: ContextMenu) {
        val position = canvas.localToScreen(
            menu.position.x,
            menu.position.y
        )
        menu.toFx().show(canvas, position.x, position.y)
    }

    override fun startClip(rectangle: Rectangle) {
        context.save()

        if (rectangle.left == 0.0 && rectangle.top == 0.0 && rectangle.width == canvas.width && rectangle.height == canvas.height) {
            return
        }
        context.beginPath()
        context.rect(rectangle.left, rectangle.top, rectangle.width, rectangle.height)
        context.clip()
    }

    override fun endClip() {
        context.restore()
    }

    fun javafx.scene.input.KeyCode.toCommon() = when (this) {
        javafx.scene.input.KeyCode.ENTER -> KeyCode.ENTER
        javafx.scene.input.KeyCode.BACK_SPACE -> KeyCode.BACKSPACE
        javafx.scene.input.KeyCode.TAB -> KeyCode.TAB
        javafx.scene.input.KeyCode.CANCEL -> KeyCode.ESCAPE
        javafx.scene.input.KeyCode.SHIFT -> KeyCode.SHIFT
        javafx.scene.input.KeyCode.CONTROL -> KeyCode.CTRL
        javafx.scene.input.KeyCode.ALT -> KeyCode.ALT
        javafx.scene.input.KeyCode.ESCAPE -> KeyCode.ESCAPE
        javafx.scene.input.KeyCode.SPACE -> KeyCode.SPACE
        javafx.scene.input.KeyCode.PAGE_UP -> KeyCode.PAGE_UP
        javafx.scene.input.KeyCode.PAGE_DOWN -> KeyCode.PAGE_DOWN
        javafx.scene.input.KeyCode.END -> KeyCode.END
        javafx.scene.input.KeyCode.HOME -> KeyCode.HOME
        javafx.scene.input.KeyCode.LEFT -> KeyCode.ARROW_LEFT
        javafx.scene.input.KeyCode.UP -> KeyCode.ARROW_UP
        javafx.scene.input.KeyCode.RIGHT -> KeyCode.ARROW_RIGHT
        javafx.scene.input.KeyCode.DOWN -> KeyCode.ARROW_DOWN
        javafx.scene.input.KeyCode.COMMA -> KeyCode.COMMA
        javafx.scene.input.KeyCode.MINUS -> KeyCode.MINUS
        javafx.scene.input.KeyCode.PERIOD -> KeyCode.PERIOD
        javafx.scene.input.KeyCode.SLASH -> KeyCode.SLASH
        javafx.scene.input.KeyCode.DIGIT0, javafx.scene.input.KeyCode.NUMPAD0 -> KeyCode.NUM_0
        javafx.scene.input.KeyCode.DIGIT1, javafx.scene.input.KeyCode.NUMPAD1 -> KeyCode.NUM_1
        javafx.scene.input.KeyCode.DIGIT2, javafx.scene.input.KeyCode.NUMPAD2 -> KeyCode.NUM_2
        javafx.scene.input.KeyCode.DIGIT3, javafx.scene.input.KeyCode.NUMPAD3 -> KeyCode.NUM_3
        javafx.scene.input.KeyCode.DIGIT4, javafx.scene.input.KeyCode.NUMPAD4 -> KeyCode.NUM_4
        javafx.scene.input.KeyCode.DIGIT5, javafx.scene.input.KeyCode.NUMPAD5 -> KeyCode.NUM_5
        javafx.scene.input.KeyCode.DIGIT6, javafx.scene.input.KeyCode.NUMPAD6 -> KeyCode.NUM_6
        javafx.scene.input.KeyCode.DIGIT7, javafx.scene.input.KeyCode.NUMPAD7 -> KeyCode.NUM_7
        javafx.scene.input.KeyCode.DIGIT8, javafx.scene.input.KeyCode.NUMPAD8 -> KeyCode.NUM_8
        javafx.scene.input.KeyCode.DIGIT9, javafx.scene.input.KeyCode.NUMPAD9 -> KeyCode.NUM_9
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
        javafx.scene.input.KeyCode.OPEN_BRACKET -> KeyCode.ROUND_BRACKET_LEFT
        javafx.scene.input.KeyCode.BACK_SLASH -> KeyCode.BACKSLASH
        javafx.scene.input.KeyCode.CLOSE_BRACKET -> KeyCode.ROUND_BRACKET_RIGHT
        javafx.scene.input.KeyCode.MULTIPLY -> KeyCode.MULTIPLY
        javafx.scene.input.KeyCode.ADD -> KeyCode.PLUS
        javafx.scene.input.KeyCode.SEPARATOR -> KeyCode.COMMA
        javafx.scene.input.KeyCode.SUBTRACT -> KeyCode.MINUS
        javafx.scene.input.KeyCode.DECIMAL -> KeyCode.PERIOD
        javafx.scene.input.KeyCode.DIVIDE -> KeyCode.SLASH
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
        javafx.scene.input.KeyCode.PRINTSCREEN -> KeyCode.PRINT
        javafx.scene.input.KeyCode.INSERT -> KeyCode.INSERT
        javafx.scene.input.KeyCode.QUOTE -> KeyCode.QUOTE
        javafx.scene.input.KeyCode.ASTERISK -> KeyCode.MULTIPLY
        javafx.scene.input.KeyCode.QUOTEDBL -> KeyCode.DOUBLE_QUOTE
        javafx.scene.input.KeyCode.LESS -> KeyCode.ANGLE_BRACKET_LEFT
        javafx.scene.input.KeyCode.GREATER -> KeyCode.ANGLE_BRACKET_RIGHT
        javafx.scene.input.KeyCode.BRACELEFT -> KeyCode.SQUARE_BRACKET_LEFT
        javafx.scene.input.KeyCode.BRACERIGHT -> KeyCode.SQUARE_BRACKET_RIGHT
        javafx.scene.input.KeyCode.AT -> KeyCode.AT
        javafx.scene.input.KeyCode.COLON -> KeyCode.COLON
        javafx.scene.input.KeyCode.DOLLAR -> KeyCode.DOLLAR
        javafx.scene.input.KeyCode.EURO_SIGN -> KeyCode.EURO
        javafx.scene.input.KeyCode.EXCLAMATION_MARK -> KeyCode.EXCLAMATION_MARK
        javafx.scene.input.KeyCode.LEFT_PARENTHESIS -> KeyCode.CURLY_BRACKET_LEFT
        javafx.scene.input.KeyCode.PLUS -> KeyCode.PLUS
        javafx.scene.input.KeyCode.RIGHT_PARENTHESIS -> KeyCode.CURLY_BRACKET_RIGHT
        javafx.scene.input.KeyCode.UNDERSCORE -> KeyCode.UNDERSCORE
        javafx.scene.input.KeyCode.CUT -> KeyCode.CUT
        javafx.scene.input.KeyCode.COPY -> KeyCode.COPY
        javafx.scene.input.KeyCode.PASTE -> KeyCode.PASTE
        javafx.scene.input.KeyCode.UNDO -> KeyCode.UNDO
        javafx.scene.input.KeyCode.AGAIN -> KeyCode.REDO
        javafx.scene.input.KeyCode.FIND -> KeyCode.FIND
        else -> {
            logger.info { "Unsupported keyCode: ${this.code}" }
            null
        }
    }

    init {
        context.lineCap = StrokeLineCap.BUTT
        context.lineJoin = StrokeLineJoin.MITER
        canvas.isFocusTraversable = true


        canvas.setOnMousePressed { event ->
            canvas.requestFocus()
            if (event.button == MouseButton.PRIMARY) {
                listenerManager.onPointerDown(
                    PointerEvent(
                        Point(event.x, event.y),
                        dimension,
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                    )
                )
            }
        }
        canvas.setOnMouseReleased { event ->
            if (event.button == MouseButton.PRIMARY) {
                listenerManager.onPointerUp(
                    PointerEvent(
                        Point(event.x, event.y),
                        dimension,
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                    )
                )
            }
        }
        canvas.setOnMouseDragged { event ->
            listenerManager.onPointerDrag(
                PointerEvent(
                    Point(event.x, event.y),
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnMouseMoved { event ->
            listenerManager.onPointerMove(
                PointerEvent(
                    Point(event.x, event.y),
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnMouseClicked { event ->
            when (event.button) {
                MouseButton.SECONDARY -> listenerManager.onPointerSecondaryAction(
                    PointerEvent(
                        Point(event.x, event.y),
                        dimension,
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                    )
                )
                MouseButton.FORWARD -> listenerManager.onKeyPress(
                    KeyEvent(
                        KeyCode.REDO,
                        "",
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                    )
                )
                MouseButton.BACK -> listenerManager.onKeyPress(
                    KeyEvent(
                        KeyCode.UNDO,
                        "",
                        event.isControlDown,
                        event.isAltDown,
                        event.isShiftDown
                    )
                )
                else -> {
                }
            }
        }
        canvas.setOnMouseEntered { event ->
            listenerManager.onPointerEnter(
                PointerEvent(
                    Point(event.x, event.y),
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnMouseExited { event ->
            listenerManager.onPointerLeave(
                PointerEvent(
                    Point(event.x, event.y),
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnScroll { event ->
            listenerManager.onScroll(
                ScrollEvent(
                    Point(event.x, event.y),
                    Point(event.deltaX, event.deltaY),
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnZoom { event ->
            listenerManager.onZoom(
                ZoomEvent(
                    Point(event.x, event.y),
                    event.zoomFactor,
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnRotate { event ->
            listenerManager.onRotate(
                RotateEvent(
                    Point(event.x, event.y),
                    event.angle / 180.0 * PI,
                    dimension,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.addDrawHook {
            listenerManager.onResize(dimension)
        }
        canvas.setOnKeyPressed { event ->
            val code = event.code.toCommon() ?: return@setOnKeyPressed
            event.consume()
            listenerManager.onKeyPress(
                KeyEvent(
                    code,
                    event.text,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
        canvas.setOnKeyReleased { event ->
            val code = event.code.toCommon() ?: return@setOnKeyReleased
            event.consume()
            listenerManager.onKeyRelease(
                KeyEvent(
                    code,
                    event.text,
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
                )
            )
        }
    }
}

fun Color.toFx(): javafx.scene.paint.Color = javafx.scene.paint.Color.rgb(red, green, blue, alpha)