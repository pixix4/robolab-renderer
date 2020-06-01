package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TextView(
    center: Point,
    private val fontSize: Double,
    initText: String,
    color: ViewColor,
    private val alignment: ICanvas.FontAlignment,
    private val fontWeight: ICanvas.FontWeight,
    private val changeCallback: (String) -> Boolean = { false }
) : BaseView() {

    val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Point, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    val textProperty = property(initText)
    var text by textProperty
    private var cursor: Cursor = Cursor()
        set(value) {
            field = value.checkBoundary(text)
        }

    data class Cursor(val line: Int = 0, val char: Int = 0) {
        fun checkBoundary(text: String): Cursor {
            val lines = text.split("\n")

            val line = max(0, min(lines.lastIndex, line))
            val char = max(0, min(lines[line].length, char))

            return Cursor(line, char)
        }

        fun index(text: String): Int {
            val lines = text.split("\n")

            var index = 0

            for (i in 0 until line) {
                index += lines[i].length + 1
            }
            index += char

            return max(0, min(text.length, index))
        }
    }

    override fun onDraw(context: DrawContext) {
        val color = context.c(color)
        if (focusable && (isHovered || isFocused)) {
            context.fillText(text, center, color.a(0.15), fontSize, alignment, fontWeight)
            context.fillRect(box, color.a(0.15))

            val lines = text.split("\n")

            val charWidth = fontSize / CHAR_WIDTH
            val charHeight = fontSize / CHAR_HEIGHT
            val charIterator = Point(charWidth, 0.0)

            for ((lineIndex, line) in lines.withIndex()) {
                val start = Point(box.left + charWidth, box.bottom - charHeight / 2 - lineIndex * charHeight)

                if (isFocused && lineIndex == cursor.line) {
                    val cursorPosition = start + charIterator * cursor.char

                    context.strokeLine(
                        listOf(
                            Point(cursorPosition.left, start.top),
                            Point(cursorPosition.left, start.top - charHeight)
                        ), color, PlottingConstraints.LINE_WIDTH / 2
                    )
                }

                var postion = start + Point(charWidth / 2, -charHeight / 2)
                for (char in line) {
                    context.fillText(char.toString(), postion, color, fontSize, alignment, fontWeight)
                    postion += charIterator
                }
            }
            if (isFocused) {
                context.strokeLine(
                    listOf(
                        box.topLeft, box.topRight
                    ), color, PlottingConstraints.LINE_WIDTH
                )
            }
        } else {
            context.fillText(text, center, color, fontSize, alignment, fontWeight)
        }

        super.onDraw(context)
    }

    private var box = Rectangle.ZERO
    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()

        val lines = text.split("\n")

        val width = fontSize / CHAR_WIDTH * ((lines.maxBy { it.length }?.length ?: 0) + 2)
        val height = fontSize / CHAR_HEIGHT * (lines.size + 0.8)
        box = Rectangle(
            center.left - width / 2,
            center.top - height / 2.2,
            width,
            height
        )
        return box unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return planetPoint in box
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(center, text.replace("\n", "\\n"))
    }

    init {
        onPointerDown { event ->
            val lines = text.split("\n")

            val charCount = lines.maxBy { it.length }?.length ?: 0
            val lineCount = lines.size

            val textBox = box.shrink(0.0, fontSize / CHAR_WIDTH)

            val left = event.planetPoint.left - textBox.left
            val top =  textBox.bottom - event.planetPoint.top - fontSize / CHAR_HEIGHT / 2

            val charSize = Point(
                1.0 / charCount * textBox.width,
                1.0 / lineCount * textBox.height
            )

            cursor = Cursor(
                (top / charSize.height).roundToInt(),
                (left / charSize.width).roundToInt()
            )

            requestRedraw()
        }

        onKeyPress { event ->
            if (!isFocused) return@onKeyPress
            val index = cursor.index(text)

            when (event.keyCode) {
                KeyCode.BACKSPACE -> {
                    if (index > 0) {
                        val oldChar = text[index]
                        val newText = text.substring(0, index - 1) + text.substring(index, text.length)
                        val c = cursor
                        if (changeCallback(newText)) {
                            text = newText
                            if (c == cursor) {
                                if (oldChar == '\n') {
                                    cursor = cursor.copy(line = cursor.line - 1, char = Int.MAX_VALUE)
                                } else {
                                    cursor = cursor.copy(char = cursor.char - 1)
                                }
                            }
                        }
                    }
                }
                KeyCode.DELETE -> {
                    if (index < text.length) {
                        val newText = text.substring(0, index) + text.substring(index + 1, text.length)
                        if (changeCallback(newText)) {
                            text = newText
                        }
                    }
                }
                KeyCode.ARROW_LEFT -> {
                    cursor = cursor.copy(char = cursor.char - 1)
                }
                KeyCode.ARROW_RIGHT -> {
                    cursor = cursor.copy(char = cursor.char + 1)
                }
                KeyCode.ARROW_UP -> {
                    cursor = cursor.copy(line = cursor.line - 1)
                }
                KeyCode.ARROW_DOWN -> {
                    cursor = cursor.copy(line = cursor.line + 1)
                }
                KeyCode.END -> {
                    cursor = if (event.ctrlKey) {
                        Cursor(Int.MAX_VALUE, Int.MAX_VALUE)
                    } else {
                        cursor.copy(char = Int.MAX_VALUE)
                    }
                }
                KeyCode.HOME -> {
                    cursor = if (event.ctrlKey) {
                        Cursor(0, 0)
                    } else {
                        cursor.copy(char = 0)
                    }
                }
                KeyCode.ENTER -> {
                    val newText = text.substring(0, index) + "\n" + text.substring(index, text.length)
                    if (changeCallback(newText)) {
                        text = newText
                        cursor = cursor.copy(line = cursor.line + 1, char = 0)
                    }
                }
                else -> {
                    var c = event.keyCode.char ?: return@onKeyPress

                    if (!event.shiftKey) {
                        c = c.toLowerCase()
                    }

                    val newText = text.substring(0, index) + c + text.substring(index, text.length)
                    if (changeCallback(newText)) {
                        text = newText
                        cursor = cursor.copy(char = cursor.char + 1)
                    }
                }
            }

            requestRedraw()
            event.stopPropagation()
        }

        textProperty.onChange {
            cursor = cursor
        }
    }

    companion object {
        private const val CHAR_WIDTH: Int = 140
        private const val CHAR_HEIGHT: Int = 100
    }
}
