package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.MirrorPointCanvas
import de.robolab.client.renderer.canvas.TransformationCanvas
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.utils.ITransformation
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TextView(
    initSource: Vector,
    private val fontSize: Double,
    initText: String,
    color: ViewColor,
    var alignment: ICanvas.FontAlignment,
    private val fontWeight: ICanvas.FontWeight,
    private val changeCallback: (String) -> Boolean = { false }
) : BaseView() {

    val sourceTransition = transition(initSource)
    val source by sourceTransition
    fun setSource(source: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        sourceTransition.animate(source, duration, offset)
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

    data class AttributeLine(
        val line: String,
        val index: Int,
        val box: Rectangle
    ) {
        fun cursorPositionToBoxLeft(innerBox: Rectangle, char: Int): Double {
            return box.width * char / line.length + box.left - innerBox.left
        }
        fun boxLeftToCursorPosition(innerBox: Rectangle, left: Double): Int {
            return ((left + innerBox.left - box.left) * line.length / box.width).roundToInt()
        }
     }

    private var innerBox: Rectangle = Rectangle.ZERO
    private var outerBox: Rectangle = Rectangle.ZERO
    private fun calcLineBoxes(): List<AttributeLine> {
        val lines = text.split("\n")

        val charWidth = fontSize / CHAR_WIDTH
        val charHeight = fontSize / CHAR_HEIGHT

        val maxLineCharCount = lines.maxByOrNull { it.length }?.length ?: 0

        val width = charWidth * (lines.maxByOrNull { it.length }?.length ?: 0)
        val height = charHeight * lines.size

        innerBox = Rectangle(
            when (alignment) {
                ICanvas.FontAlignment.LEFT -> source.left
                ICanvas.FontAlignment.CENTER -> source.left - width / 2
                ICanvas.FontAlignment.RIGHT -> source.left - width
            },
            source.top - height / 2.0,
            width,
            height
        )
        outerBox = innerBox.expand(0.4 * charHeight, charWidth)

        return lines.mapIndexed { lineIndex, line ->
            val lineWidth = innerBox.width * (line.length / maxLineCharCount.toDouble())
            AttributeLine(
                line,
                lineIndex,
                Rectangle(
                    when (alignment) {
                        ICanvas.FontAlignment.LEFT -> innerBox.left
                        ICanvas.FontAlignment.CENTER -> innerBox.left + (innerBox.width - lineWidth) / 2
                        ICanvas.FontAlignment.RIGHT -> innerBox.left + (innerBox.width - lineWidth)
                    },
                    innerBox.bottom - charHeight * (lineIndex + 1),
                    lineWidth,
                    charHeight
                )
            )
        }
    }

    override fun onDraw(context: DrawContext) {
        val color = context.c(color)
        val cursorColor = context.theme.plotter.editColor

        val transformation: ITransformation = Transformation(
            gridWidth = 1.0,
            pixelPerUnitDimension = Dimension.ONE
        )
        transformation.rotateTo(-context.transformation.rotation, innerBox.center)
        val flip = context.transformation.flipView

        val tCanvas = TransformationCanvas(context, transformation)

        val canvas = if (flip) {
            MirrorPointCanvas(tCanvas, outerBox.center.x)
        } else tCanvas

        val lineBoxes = calcLineBoxes()
        if (focusable && (isHovered || isFocused)) {
            canvas.fillRect(outerBox, color.a(0.15))
        }

        val charWidth = fontSize / CHAR_WIDTH
        val charHeight = fontSize / CHAR_HEIGHT
        val charIterator = Vector(charWidth, 0.0)

        for ((line, index, box) in lineBoxes) {
            if (isFocused && index == cursor.line) {

                val cursorPosition = box.topLeft + charIterator * cursor.char

                canvas.strokeLine(
                    listOf(
                        Vector(cursorPosition.left, box.top),
                        Vector(cursorPosition.left, box.bottom)
                    ), cursorColor, PlottingConstraints.LINE_WIDTH / 2
                )
            }

            var iterator = box.topLeft + Vector(charWidth / 2, charHeight / 2)
            for (char in line) {
                canvas.fillText(char.toString(), iterator, color, fontSize, ICanvas.FontAlignment.CENTER, fontWeight)
                iterator += charIterator
            }
        }

        if (isFocused) {
            canvas.strokeLine(
                listOf(
                    outerBox.topLeft, outerBox.topRight
                ), cursorColor, PlottingConstraints.LINE_WIDTH
            )
        }

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()

        calcLineBoxes()
        return outerBox unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return planetPoint in outerBox
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(source, text.replace("\n", "\\n"))
    }

    init {
        onPointerDown { event ->

            for ((line, index, box) in calcLineBoxes()) {
                if (event.planetPoint in box) {
                    cursor = Cursor(
                        index,
                        ((event.planetPoint.left - box.left) / box.width * line.length).roundToInt()
                    )
                }
            }

            requestRedraw()
        }

        onKeyPress { event ->
            if (!isFocused) return@onKeyPress
            val text = text
            val index = cursor.index(text)

            when (event.keyCode) {
                KeyCode.BACKSPACE -> {
                    if (index > 0) {
                        val oldChar = text.getOrNull(index - 1)
                        val newText = text.substring(0, index - 1) + text.substring(index, text.length)
                        val c = cursor
                        if (changeCallback(newText)) {
                            this.text = newText
                            cursor = if (oldChar == '\n') {
                                c.copy(line = c.line - 1, char = Int.MAX_VALUE)
                            } else {
                                c.copy(char = c.char - 1)
                            }
                        }
                    }
                }
                KeyCode.DELETE -> {
                    if (index < text.length) {
                        val newText = text.substring(0, index) + text.substring(index + 1, text.length)
                        if (changeCallback(newText)) {
                            this.text = newText
                        }
                    }
                }
                KeyCode.ARROW_LEFT -> {
                    cursor = if (cursor.char == 0 && cursor.line > 0) {
                        Cursor(cursor.line - 1, Int.MAX_VALUE)
                    } else {
                        cursor.copy(char = cursor.char - 1)
                    }
                }
                KeyCode.ARROW_RIGHT -> {
                    val lines = text.split('\n')
                    val line = lines.getOrNull(cursor.line) ?: ""
                    cursor = if (line.length == cursor.char && cursor.line < lines.lastIndex) {
                        Cursor(cursor.line + 1, 0)
                    } else {
                        cursor.copy(char = cursor.char + 1)
                    }
                }
                KeyCode.ARROW_UP -> {
                    val lines = calcLineBoxes()
                    if (cursor.line > 0) {
                        val currentLine = lines[cursor.line]
                        val nextLine = lines.getOrNull(cursor.line - 1) ?: return@onKeyPress

                        // Magic number to fix rounding direction on 0.5 line offset to left char
                        val left = currentLine.cursorPositionToBoxLeft(innerBox, cursor.char) - 0.02
                        val char = nextLine.boxLeftToCursorPosition(innerBox, left)
                        cursor = Cursor(cursor.line - 1, char)
                    }
                }
                KeyCode.ARROW_DOWN -> {
                    val lines = calcLineBoxes()
                    if (cursor.line < lines.lastIndex) {
                        val currentLine = lines[cursor.line]
                        val nextLine = lines.getOrNull(cursor.line + 1) ?: return@onKeyPress

                        // Magic number to fix rounding direction on 0.5 line offset to right char
                        val left = currentLine.cursorPositionToBoxLeft(innerBox, cursor.char) + 0.02
                        val char = nextLine.boxLeftToCursorPosition(innerBox, left)
                        cursor = Cursor(cursor.line + 1, char)
                    }
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
                        this.text = newText
                        cursor = cursor.copy(line = cursor.line + 1, char = 0)
                    }
                }
                else -> {
                    var c = event.keyCode.char ?: return@onKeyPress

                    if (!event.shiftKey) {
                        c = c.lowercaseChar()
                    }

                    val newText = text.substring(0, index) + c + text.substring(index, text.length)
                    if (changeCallback(newText)) {
                        this.text = newText
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
