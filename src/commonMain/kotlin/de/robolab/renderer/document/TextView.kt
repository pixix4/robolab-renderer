package de.robolab.renderer.document

import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.utils.DrawContext
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
    private var cursor: Int = 0

    override fun onDraw(context: DrawContext) {
        val color = context.c(color)
        if (focusable && (isHovered || isFocused)) {
            context.fillRect(box, color.a(0.1))

            val charSize = Point(fontSize / 120, 0.0)
            val start = Point(box.left + charSize.left, box.top + box.height / 2)
            var postion = start + charSize / 2

            if (isFocused) {
                val cursorPosition = start + charSize * cursor

                context.strokeLine(listOf(
                        Point(cursorPosition.left, box.top * 0.1 + box.bottom * 0.9),
                        Point(cursorPosition.left, box.top * 0.9 + box.bottom * 0.1)
                ), color, PlottingConstraints.LINE_WIDTH / 2)

                context.strokeLine(listOf(
                        box.topLeft, box.topRight
                ), color, PlottingConstraints.LINE_WIDTH)
            }

            for (char in text) {
                context.fillText(char.toString(), postion, color, fontSize, alignment, fontWeight)
                postion += charSize
            }
        } else {
            context.fillText(text, center, color, fontSize, alignment, fontWeight)
        }

        super.onDraw(context)
    }

    private var box = Rectangle.ZERO
    override fun updateBoundingBox(): Rectangle? {
        val parentBox = super.updateBoundingBox()
        
        val width = fontSize / 120 * (text.length + 2)
        val height = fontSize / 100 * 1.8
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

    init {
        onPointerDown { event ->
            val left = event.canvasPoint.left - box.left
            val percent = left / box.width
            cursor = (percent * text.length).roundToInt()

            requestRedraw()
            event.stopPropagation()
        }

        onKeyPress { event ->
            if (!isFocused) return@onKeyPress

            when (event.keyCode) {
                KeyCode.BACKSPACE -> {
                    if (cursor > 0) {
                        val newText = text.substring(0, cursor - 1) + text.substring(cursor, text.length)
                        if (changeCallback(newText)) {
                            text = newText
                            cursor -= 1
                        }
                    }
                }
                KeyCode.DELETE -> {
                    if (cursor < text.length) {
                        val newText = text.substring(0, cursor) + text.substring(cursor + 1, text.length)
                        if (changeCallback(newText)) {
                            text = newText
                        }
                    }
                }
                KeyCode.ARROW_LEFT -> {
                    cursor = max(0, cursor - 1)
                }
                KeyCode.ARROW_RIGHT -> {
                    cursor = min(text.length, cursor + 1)
                }
                else -> {
                    var c = event.keyCode.char ?: return@onKeyPress

                    if (!event.shiftKey) {
                        c = c.toLowerCase()
                    }

                    val newText = text.substring(0, cursor) + c + text.substring(cursor, text.length)
                    if (changeCallback(newText)) {
                        text = newText
                        cursor += 1
                    }
                }
            }

            requestRedraw()
            event.stopPropagation()
        }

        textProperty.onChange {
            cursor = min(cursor, text.length)
        }
    }
}
