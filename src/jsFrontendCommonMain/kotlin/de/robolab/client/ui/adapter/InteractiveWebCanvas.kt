package de.robolab.client.ui.adapter

import de.robolab.client.renderer.events.*
import de.robolab.client.utils.electron
import de.robolab.common.utils.*
import de.westermann.kwebview.Document
import de.westermann.kwebview.components.Canvas
import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.math.PI

class InteractiveWebCanvas(canvas: Canvas) : WebCanvas(canvas) {

    private val hammer = Hammer(canvas.html, js("{}"))

    private var lastPoint: Point? = null

    private fun mouseEventToPointerEvent(event: MouseEvent): PointerEvent {
        return PointerEvent(
            pointFromEvent(event),
            dimension,
            event.ctrlKey || event.metaKey,
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
        val point =
            Point(event.clientX - canvas.offsetLeftTotal, event.clientY - canvas.offsetTopTotal)
        lastPoint = point
        return point
    }

    private fun pointFromEvent(event: HammerEvent): Point {
        val point =
            Point(event.center.x - canvas.offsetLeftTotal, event.center.y - canvas.offsetTopTotal)
        lastPoint = point
        return point
    }

    private fun pointFromEvent(event: GestureEvent): Point {
        val point =
            Point(event.clientX - canvas.offsetLeftTotal, event.clientY - canvas.offsetTopTotal)
        lastPoint = point
        return point
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
                            event.ctrlKey || event.metaKey,
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
                            event.ctrlKey || event.metaKey,
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
                    window.getComputedStyle(canvas.html).fontSize.replace("px", "").toDoubleOrNull()
                        ?.let { it * 1.2 }
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
                    event.ctrlKey || event.metaKey,
                    event.altKey,
                    event.shiftKey
                )
            )
        }

        canvas.onKeyDown { event ->
            event.stopPropagation()
            event.preventDefault()

            val e = event.toEvent()
            listenerManager.onKeyPress(e)
        }
        canvas.onKeyPress { event ->
            event.preventDefault()

            val e = event.toEvent()
            listenerManager.onKeyPress(e)

            if (!e.bubbles) {
                event.stopPropagation()
            }
        }
        canvas.onKeyUp { event ->
            event.preventDefault()

            val e = event.toEvent()
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
                            event.ctrlKey || event.metaKey,
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
                            event.ctrlKey || event.metaKey,
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

fun KeyboardEvent.toEvent(): KeyEvent {
    return KeyEvent(
        getKeyCode(),
        key,
        ctrlKey || metaKey,
        altKey,
        shiftKey
    )
}

fun KeyboardEvent.getKeyCode() = when (key.lowercase()) {
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
        KeyCode.UNSUPPORTED
    }
}
