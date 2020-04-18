package de.robolab.renderer.drawable.edit

import de.robolab.app.model.file.toFixed
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.BackgroundDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
import de.robolab.utils.PreferenceStorage
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class EditPaperBackground(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    private val enabled by PreferenceStorage.paperBackgroundEnabledProperty
    private val gridWidth by PreferenceStorage.paperGridWidthProperty
    private val paperWidth by PreferenceStorage.paperStripWidthProperty
    private val orientation by PreferenceStorage.paperOrientationProperty
    private val minimalPadding by PreferenceStorage.paperMinimalPaddingProperty
    private val precision by PreferenceStorage.paperPrecisionProperty

    private var changes = false
    private var drawData: DrawData? = null
    private var measuringSourceEdge: EditPaperEdge? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        if (changes) {
            changes = false
            return true
        }
        return false
    }

    private fun DrawContext.drawMeasuringLine(source: Point, target: Point, label: String = "") {
        val endingDirection = (target - source).let { Point(-it.y, it.x) }.normalize() * MEASURING_LINE_ENDING_WIDTH / 2
        strokeLine(
                listOf(
                        source,
                        target
                ),
                theme.lineColor,
                PlottingConstraints.LINE_WIDTH / 2
        )
        strokeLine(
                listOf(
                        source + endingDirection,
                        source - endingDirection
                ),
                theme.lineColor,
                PlottingConstraints.LINE_WIDTH / 2
        )
        strokeLine(
                listOf(
                        target + endingDirection,
                        target - endingDirection
                ),
                theme.lineColor,
                PlottingConstraints.LINE_WIDTH / 2
        )

        if (label.isNotEmpty()) {
            fillText(
                    label,
                    source.interpolate(target, 0.5) + endingDirection * 2,
                    theme.lineColor,
                    alignment = ICanvas.FontAlignment.CENTER
            )
        }
    }

    override fun onDraw(context: DrawContext) {
        val data = drawData ?: return

        // Fill paper background
        context.fillRect(data.paper, context.theme.primaryBackgroundColor)

        // Draw resize handlers
        for ((rect, edge) in data.dragRects) {
            // Mark measuring source edge
            val color = if (edge == measuringSourceEdge) {
                context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5)
            } else {
                context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.9)
            }
            context.fillRect(rect, color)
        }

        // Draw paper outline
        context.strokeRect(
                data.paper,
                context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5),
                PlottingConstraints.LINE_WIDTH
        )

        if (data.orientation == Orientation.HORIZONTAL) {
            var line = data.paper.bottom - data.lineSeparation

            // Draw paper sheet separator
            while (line > data.paper.top) {
                context.strokeLine(
                        listOf(
                                Point(data.paper.left, line),
                                Point(data.paper.right, line)
                        ),
                        context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5),
                        PlottingConstraints.LINE_WIDTH
                )
                line -= data.lineSeparation
            }

            // Draw paper sheet width marker
            context.drawMeasuringLine(
                    Point(data.paper.left - SECOND_MEASURING_LINE_PAPER_DISTANCE, data.paper.bottom - data.lineSeparation),
                    Point(data.paper.left - SECOND_MEASURING_LINE_PAPER_DISTANCE, data.paper.bottom),
                    "${paperWidth.toFixed(precision)}m"
            )
        } else {
            var line = data.paper.left + data.lineSeparation

            // Draw paper sheet separator
            while (line < data.paper.right) {
                context.strokeLine(
                        listOf(
                                Point(line, data.paper.bottom),
                                Point(line, data.paper.top)
                        ),
                        context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5),
                        PlottingConstraints.LINE_WIDTH
                )
                line += data.lineSeparation
            }

            // Draw paper sheet width marker
            context.drawMeasuringLine(
                    Point(data.paper.left, data.paper.bottom + SECOND_MEASURING_LINE_PAPER_DISTANCE),
                    Point(data.paper.left + data.lineSeparation, data.paper.bottom + SECOND_MEASURING_LINE_PAPER_DISTANCE),
                    "${paperWidth.toFixed(precision)}m"
            )
        }

        // Draw planet outline
        context.strokeRect(
                data.planet,
                context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5),
                PlottingConstraints.LINE_WIDTH
        )

        // Draw paper height marker
        val paperHeightUnits = data.paper.height * gridWidth
        context.drawMeasuringLine(
                Point(data.paper.left - FIRST_MEASURING_LINE_PAPER_DISTANCE, data.paper.top),
                Point(data.paper.left - FIRST_MEASURING_LINE_PAPER_DISTANCE, data.paper.bottom),
                "${paperHeightUnits.toFixed(precision)}m"
        )
        // Draw paper width marker
        val paperWidthUnits = data.paper.width * gridWidth
        context.drawMeasuringLine(
                Point(data.paper.left, data.paper.bottom + FIRST_MEASURING_LINE_PAPER_DISTANCE),
                Point(data.paper.right, data.paper.bottom + FIRST_MEASURING_LINE_PAPER_DISTANCE),
                "${paperWidthUnits.toFixed(precision)}m"
        )

        // Draw paper - planet vertical padding markers
        val bottomPadding = data.paper.bottom - data.planet.bottom
        val topPadding = data.planet.top - data.paper.top
        context.drawMeasuringLine(
                Point(data.planet.right + MEASURING_LINE_PLANET_DISTANCE, data.paper.bottom),
                Point(data.planet.right + MEASURING_LINE_PLANET_DISTANCE, data.paper.bottom - bottomPadding),
                "${(bottomPadding * gridWidth).toFixed(precision)}m"
        )
        context.drawMeasuringLine(
                Point(data.planet.right + MEASURING_LINE_PLANET_DISTANCE, data.paper.top + topPadding),
                Point(data.planet.right + MEASURING_LINE_PLANET_DISTANCE, data.paper.top),
                "${(topPadding * gridWidth).toFixed(precision)}m"
        )
        context.drawMeasuringLine(
                Point(data.planet.left - MEASURING_LINE_PLANET_DISTANCE, data.paper.bottom),
                Point(data.planet.left - MEASURING_LINE_PLANET_DISTANCE, data.paper.bottom - bottomPadding),
                "${(bottomPadding * gridWidth).toFixed(precision)}m"
        )
        context.drawMeasuringLine(
                Point(data.planet.left - MEASURING_LINE_PLANET_DISTANCE, data.paper.top + topPadding),
                Point(data.planet.left - MEASURING_LINE_PLANET_DISTANCE, data.paper.top),
                "${(topPadding * gridWidth).toFixed(precision)}m"
        )


        val bottomPlanetPoint = floor(data.planet.bottom).toInt()
        val topPlanetPoint = ceil(data.planet.top).toInt()
        val verticalAccumulator = DistanceAccumulator(
                if (measuringSourceEdge?.isTop == true) paperHeightUnits else 0.0,
                measuringSourceEdge == null
        )
        context.drawMeasuringLine(
                Point(data.paper.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, data.paper.bottom),
                Point(data.paper.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, bottomPlanetPoint),
                "${verticalAccumulator.accumulate((data.paper.bottom - bottomPlanetPoint) * gridWidth).toFixed(precision)}m"
        )
        for (y in (topPlanetPoint until bottomPlanetPoint).reversed()) {
            context.drawMeasuringLine(
                    Point(data.paper.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, y + 1),
                    Point(data.paper.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, y),
                    "${verticalAccumulator.accumulate(gridWidth).toFixed(precision)}m"
            )
        }
        context.drawMeasuringLine(
                Point(data.paper.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, topPlanetPoint),
                Point(data.paper.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, data.paper.top),
                "${verticalAccumulator.accumulate((topPlanetPoint - data.paper.top) * gridWidth).toFixed(precision)}m"
        )

        // Draw paper - planet horizontal padding markers
        val rightPadding = data.paper.right - data.planet.right
        val leftPadding = data.planet.left - data.paper.left
        context.drawMeasuringLine(
                Point(data.paper.right, data.planet.top - MEASURING_LINE_PLANET_DISTANCE),
                Point(data.paper.right - rightPadding, data.planet.top - MEASURING_LINE_PLANET_DISTANCE),
                "${(rightPadding * gridWidth).toFixed(precision)}m"
        )
        context.drawMeasuringLine(
                Point(data.paper.left + leftPadding, data.planet.top - MEASURING_LINE_PLANET_DISTANCE),
                Point(data.paper.left, data.planet.top - MEASURING_LINE_PLANET_DISTANCE),
                "${(leftPadding * gridWidth).toFixed(precision)}m"
        )
        context.drawMeasuringLine(
                Point(data.paper.right, data.planet.bottom + MEASURING_LINE_PLANET_DISTANCE),
                Point(data.paper.right - rightPadding, data.planet.bottom + MEASURING_LINE_PLANET_DISTANCE),
                "${(rightPadding * gridWidth).toFixed(precision)}m"
        )
        context.drawMeasuringLine(
                Point(data.paper.left + leftPadding, data.planet.bottom + MEASURING_LINE_PLANET_DISTANCE),
                Point(data.paper.left, data.planet.bottom + MEASURING_LINE_PLANET_DISTANCE),
                "${(leftPadding * gridWidth).toFixed(precision)}m"
        )

        val rightPlanetPoint = floor(data.planet.right).toInt()
        val leftPlanetPoint = ceil(data.planet.left).toInt()
        val horizontalAccumulator = DistanceAccumulator(
                if (measuringSourceEdge?.isLeft == true) paperWidthUnits else 0.0,
                measuringSourceEdge == null
        )
        context.drawMeasuringLine(
                Point(data.paper.right, data.paper.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                Point(rightPlanetPoint, data.paper.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                "${horizontalAccumulator.accumulate((data.paper.right - rightPlanetPoint) * gridWidth).toFixed(precision)}m"
        )
        for (x in (leftPlanetPoint until rightPlanetPoint).reversed()) {
            context.drawMeasuringLine(
                    Point(x + 1, data.paper.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                    Point(x, data.paper.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                    "${horizontalAccumulator.accumulate(gridWidth).toFixed(precision)}m"
            )
        }
        context.drawMeasuringLine(
                Point(leftPlanetPoint, data.paper.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                Point(data.paper.left, data.paper.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                "${horizontalAccumulator.accumulate((leftPlanetPoint - data.paper.left) * gridWidth).toFixed(precision)}m"
        )
    }

    enum class EditPaperEdge(
            val isLeft: Boolean,
            val isTop: Boolean
    ) {
        LEFT_TOP(true, true),
        RIGHT_TOP(false, true),
        RIGHT_BOTTOM(false, false),
        LEFT_BOTTOM(true, false);

        val isRight = !isLeft
        val isBottom = !isTop
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val data = drawData ?: return emptyList()
        val rect = data.dragRects.keys.find { position in it } ?: return emptyList()
        val edge = data.dragRects[rect] ?: return emptyList()
        return listOf(edge)
    }

    private var lastDragPosition: Point? = null
    private var dragEdge = EditPaperEdge.LEFT_TOP
    override fun onPointerDown(event: PointerEvent, position: Point): Boolean {
        val edge = editPlanetDrawable.pointer?.findObjectUnderPointer<EditPaperEdge>()
        if (edge != null) {
            dragEdge = edge
            lastDragPosition = position
            return true
        }
        return false
    }

    private var planetOffset: Point? = null
    private var planetSize: Point? = null
    override fun onPointerDrag(event: PointerEvent, position: Point): Boolean {
        val last = lastDragPosition ?: return false

        if (event.ctrlKey || event.altKey || event.shiftKey) {
            val size = planetSize ?: Point.ZERO
            planetSize = size + (last - position) * when (dragEdge) {
                EditPaperEdge.LEFT_TOP -> Point(1.0, 1.0)
                EditPaperEdge.RIGHT_TOP -> Point(-1.0, 1.0)
                EditPaperEdge.RIGHT_BOTTOM -> Point(-1.0, -1.0)
                EditPaperEdge.LEFT_BOTTOM -> Point(1.0, -1.0)
            }
        } else {
            val offset = planetOffset ?: Point.ZERO
            planetOffset = offset + last - position
        }
        update()
        lastDragPosition = position

        return true
    }

    override fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        lastDragPosition ?: return false
        lastDragPosition = null
        return true
    }

    override fun onPointerSecondaryAction(event: PointerEvent, position: Point): Boolean {
        val edge = editPlanetDrawable.pointer?.findObjectUnderPointer<EditPaperEdge>()
        if (edge != null) {

            measuringSourceEdge = if (edge == measuringSourceEdge) {
                null
            } else {
                edge
            }
            changes = true

            return true
        }
        return false
    }

    private var area: Rectangle = Rectangle.ZERO
    private fun update() {
        if (!enabled) {
            drawData = null
            changes = true
            return
        }

        val planetPaperWidth = paperWidth / gridWidth
        val planetMinimalPadding = minimalPadding / gridWidth

        val planetArea = area.expand(PlottingConstraints.POINT_SIZE / 2)
        var paperArea = planetArea.expand(planetMinimalPadding).expand(
                max(0.0, planetSize?.top ?: 0.0),
                max(0.0, planetSize?.left ?: 0.0)
        )

        paperArea = if (orientation == Orientation.VERTICAL) {
            val expectedWidth = ceil(paperArea.width / planetPaperWidth) * planetPaperWidth
            val expandWidth = expectedWidth - paperArea.width
            paperArea.expand(0.0, expandWidth / 2)
        } else {
            val expectedHeight = ceil(paperArea.height / planetPaperWidth) * planetPaperWidth
            val expandHeight = expectedHeight - paperArea.height
            paperArea.expand(expandHeight / 2, 0.0)
        }

        val offset = planetOffset
        if (offset != null) {
            val leftOffset = (paperArea.width - planetArea.width)  / 2 - planetMinimalPadding
            val topOffset = (paperArea.height - planetArea.height)  / 2 - planetMinimalPadding

            paperArea = paperArea.copy(
                    left = paperArea.left - min(leftOffset, max(-leftOffset, offset.left)),
                    top = paperArea.top - min(topOffset, max(-topOffset, offset.top))
            )
        }

        drawData = DrawData(
                paperArea,
                planetArea,
                orientation,
                planetPaperWidth
        )
        changes = true
    }

    fun importPlanet(planet: Planet) {
        val newArea = BackgroundDrawable.calcPlanetArea(planet) ?: Rectangle.ZERO
        if (newArea != area) {
            area = newArea
            planetSize = null
            planetOffset = null
            update()
        }
    }

    init {
        PreferenceStorage.paperBackgroundEnabledProperty.onChange { update() }
        PreferenceStorage.paperGridWidthProperty.onChange { update() }
        PreferenceStorage.paperStripWidthProperty.onChange { update() }
        PreferenceStorage.paperMinimalPaddingProperty.onChange { update() }
        PreferenceStorage.paperPrecisionProperty.onChange { update() }
        PreferenceStorage.paperOrientationProperty.onChange {
            planetSize = null
            planetOffset = null
            update()
        }
    }

    private data class DrawData(
            val paper: Rectangle,
            val planet: Rectangle,
            val orientation: Orientation,
            val lineSeparation: Double
    ) {

        private val dragRectSize = 0.2
        val dragRects = mapOf(
                Rectangle(paper.left, paper.top, dragRectSize, dragRectSize) to EditPaperEdge.LEFT_TOP,
                Rectangle(paper.left, paper.bottom - dragRectSize, dragRectSize, dragRectSize) to EditPaperEdge.LEFT_BOTTOM,
                Rectangle(paper.right - dragRectSize, paper.top, dragRectSize, dragRectSize) to EditPaperEdge.RIGHT_TOP,
                Rectangle(paper.right - dragRectSize, paper.bottom - dragRectSize, dragRectSize, dragRectSize) to EditPaperEdge.RIGHT_BOTTOM
        )
    }

    private class DistanceAccumulator(
            var current: Double,
            val doNotAccumulate: Boolean = false
    ) {

        private val inverse = current != 0.0

        fun accumulate(value: Double): Double {
            val old = current
            val result = current + if (inverse) -value else value
            if (!doNotAccumulate) {
                current = result
            }

            return if (inverse) old else result
        }
    }

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }

    companion object {
        private const val MEASURING_LINE_ENDING_WIDTH: Double = 0.12

        private const val FIRST_MEASURING_LINE_PAPER_DISTANCE: Double = 0.1
        private const val SECOND_MEASURING_LINE_PAPER_DISTANCE: Double = FIRST_MEASURING_LINE_PAPER_DISTANCE + MEASURING_LINE_ENDING_WIDTH / 2

        private const val MEASURING_LINE_PLANET_DISTANCE: Double = -MEASURING_LINE_ENDING_WIDTH / 2
    }
}
