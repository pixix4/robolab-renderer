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

    private var changes = false
    private var drawData: DrawData? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        if (changes) {
            changes = false
            return true
        }
        return false
    }

    private fun DrawContext.drawMeasuringLine(source: Point, target: Point, label: String = "") {
        val endingDirection = (target - source).let { Point(-it.y, it.x) }.normalize() * 0.06
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

        context.fillRect(data.paper, context.theme.primaryBackgroundColor)
        for (rect in data.dragRects) {
            context.fillRect(rect, context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.8))
        }

        context.strokeRect(
                data.paper,
                context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5),
                PlottingConstraints.LINE_WIDTH
        )

        if (data.orientation == Orientation.HORIZONTAL) {
            var line = data.paper.bottom - data.lineSeparation

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

            context.drawMeasuringLine(
                    Point(data.paper.left - 0.16, data.paper.bottom - data.lineSeparation),
                    Point(data.paper.left - 0.16, data.paper.bottom),
                    "${paperWidth.toFixed(2)}m"
            )
        } else {
            var line = data.paper.left + data.lineSeparation

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

            context.drawMeasuringLine(
                    Point(data.paper.left, data.paper.bottom + 0.16),
                    Point(data.paper.left + data.lineSeparation, data.paper.bottom + 0.16),
                    "${paperWidth.toFixed(2)}m"
            )
        }

        context.strokeRect(
                data.planet,
                context.theme.lineColor.interpolate(context.theme.primaryBackgroundColor, 0.5),
                PlottingConstraints.LINE_WIDTH
        )

        context.drawMeasuringLine(
                Point(data.paper.left - 0.1, data.paper.top),
                Point(data.paper.left - 0.1, data.paper.bottom),
                "${(data.paper.height * gridWidth).toFixed(2)}m"
        )
        context.drawMeasuringLine(
                Point(data.paper.left, data.paper.bottom + 0.1),
                Point(data.paper.right, data.paper.bottom + 0.1),
                "${(data.paper.width * gridWidth).toFixed(2)}m"
        )

        val bottomPadding = data.paper.bottom - data.planet.bottom
        context.drawMeasuringLine(
                Point(data.paper.right + 0.1, data.paper.bottom),
                Point(data.paper.right + 0.1, data.paper.bottom - bottomPadding),
                "${(bottomPadding * gridWidth).toFixed(2)}m"
        )
        val topPadding = data.planet.top - data.paper.top
        context.drawMeasuringLine(
                Point(data.paper.right + 0.1, data.paper.top + topPadding),
                Point(data.paper.right + 0.1, data.paper.top),
                "${(topPadding * gridWidth).toFixed(2)}m"
        )

        val rightPadding = data.paper.right - data.planet.right
        context.drawMeasuringLine(
                Point(data.paper.right, data.paper.top - 0.1),
                Point(data.paper.right - rightPadding, data.paper.top - 0.1),
                "${(rightPadding * gridWidth).toFixed(2)}m"
        )
        val leftPadding = data.planet.left - data.paper.left
        context.drawMeasuringLine(
                Point(data.paper.left + leftPadding, data.paper.top - 0.1),
                Point(data.paper.left, data.paper.top - 0.1),
                "${(leftPadding * gridWidth).toFixed(2)}m"
        )
    }

    object EditPaperEdge
    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (drawData?.dragRects?.any {position in it} == true) {
            return listOf(EditPaperEdge)
        }
        return emptyList()
    }

    private var lastDragPosition: Point? = null
    override fun onPointerDown(event: PointerEvent, position: Point): Boolean {
        if (editPlanetDrawable.pointer?.findObjectUnderPointer<EditPaperEdge>() != null) {
            lastDragPosition = position
            return true
        }
        return false
    }

    private var planetOffset: Point? = null
    override fun onPointerDrag(event: PointerEvent, position: Point): Boolean {
        val last = lastDragPosition ?: return false

        val offset = planetOffset ?: Point.ZERO
        planetOffset = offset+ last - position
        update()
        lastDragPosition = position

        return true
    }

    override fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        lastDragPosition ?: return false
        lastDragPosition = null
        return true
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
        var paperArea = planetArea.expand(planetMinimalPadding)

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
            planetOffset = null
            update()
        }
    }

    init {
        PreferenceStorage.paperBackgroundEnabledProperty.onChange { update() }
        PreferenceStorage.paperGridWidthProperty.onChange { update() }
        PreferenceStorage.paperStripWidthProperty.onChange { update() }
        PreferenceStorage.paperOrientationProperty.onChange { update() }
        PreferenceStorage.paperMinimalPaddingProperty.onChange { update() }
    }

    private data class DrawData(
            val paper: Rectangle,
            val planet: Rectangle,
            val orientation: Orientation,
            val lineSeparation: Double
    ) {

        private val dragRectSize = 0.2
        val dragRects: List<Rectangle> = listOf(
                Rectangle(paper.left, paper.top, dragRectSize, dragRectSize),
                Rectangle(paper.left, paper.bottom - dragRectSize, dragRectSize, dragRectSize),
                Rectangle(paper.right - dragRectSize, paper.top, dragRectSize, dragRectSize),
                Rectangle(paper.right - dragRectSize, paper.bottom - dragRectSize, dragRectSize, dragRectSize)
        )
    }

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }
}
