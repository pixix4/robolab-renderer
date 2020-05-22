package de.robolab.client.renderer.drawable.edit

import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.GroupView
import de.robolab.client.renderer.view.component.LineView
import de.robolab.client.renderer.view.component.MeasuringLineView
import de.robolab.client.renderer.view.component.RectangleView
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class EditPaperBackground {

    private val gridWidth by PreferenceStorage.paperGridWidthProperty
    private val paperWidth by PreferenceStorage.paperStripWidthProperty
    private val orientation by PreferenceStorage.paperOrientationProperty
    private val minimalPadding by PreferenceStorage.paperMinimalPaddingProperty


    private var planetOffset: Point? = null
    private var planetSize: Point? = null


    val backgroundView = RectangleView(
        null,
        ViewColor.PRIMARY_BACKGROUND_COLOR
    ).also {
        it.animationTime = 0.0
    }

    private enum class Edge(val multiplier: Point, val isLeft: Boolean, val isTop: Boolean) {
        TOP_LEFT(Point(1.0, 1.0), true, true),
        BOTTOM_LEFT(Point(1.0, -1.0), true, false),
        TOP_RIGHT(Point(-1.0, 1.0), false, true),
        BOTTOM_RIGHT(Point(-1.0, -1.0), false, false);

        fun getArea(paperArea: Rectangle, rectSize: Double): Rectangle {
            return Rectangle.fromDimension(
                when (this) {
                    TOP_LEFT -> paperArea.topLeft
                    BOTTOM_LEFT -> paperArea.bottomLeft
                    TOP_RIGHT -> paperArea.topRight
                    BOTTOM_RIGHT -> paperArea.bottomRight
                },
                Point(rectSize, rectSize) * multiplier
            )
        }
    }

    private var selectedEdge: Edge? = null

    private val edgeList = mutableListOf<Pair<RectangleView, Edge>>()
    private fun setupHandler(view: RectangleView, edge: Edge) {
        backgroundView += view
        edgeList += view to edge

        var lastPosition = Point.ZERO
        view.onPointerDown { event ->
            lastPosition = event.planetPoint
        }
        view.onPointerDrag { event ->
            event.stopPropagation()

            val delta = lastPosition - event.planetPoint
            if (event.ctrlKey || event.altKey) {
                val size = planetSize ?: Point.ZERO
                var newSize = size + delta * edge.multiplier

                if (!event.shiftKey) {
                    newSize = newSize.max(Point.ZERO)
                }

                planetSize = newSize
            } else {
                val offset = planetOffset ?: Point.ZERO
                planetOffset = offset + delta
            }
            update()
            lastPosition = event.planetPoint
        }
        view.onPointerSecondaryAction {
            selectedEdge = if (selectedEdge == edge) null else edge
            update()
        }
    }

    private val topLeftHandler = RectangleView(
        null,
        ViewColor.PRIMARY_BACKGROUND_COLOR.interpolate(ViewColor.LINE_COLOR, 0.1)
    ).also { view ->
        setupHandler(view, Edge.TOP_LEFT)
    }
    private val bottomLeftHandler = RectangleView(
        null,
        ViewColor.PRIMARY_BACKGROUND_COLOR.interpolate(ViewColor.LINE_COLOR, 0.1)
    ).also { view ->
        setupHandler(view, Edge.BOTTOM_LEFT)
    }
    private val topRightHandler = RectangleView(
        null,
        ViewColor.PRIMARY_BACKGROUND_COLOR.interpolate(ViewColor.LINE_COLOR, 0.1)
    ).also { view ->
        setupHandler(view, Edge.TOP_RIGHT)
    }
    private val bottomRightHandler = RectangleView(
        null,
        ViewColor.PRIMARY_BACKGROUND_COLOR.interpolate(ViewColor.LINE_COLOR, 0.1)
    ).also { view ->
        setupHandler(view, Edge.BOTTOM_RIGHT)
    }

    val measuringView = GroupView("Measuring view").also {
        it.animationTime = 0.0
    }


    private var area: Rectangle = Rectangle.ZERO
    private fun update() {
        val planetPaperWidth = paperWidth / gridWidth
        val planetMinimalPadding = minimalPadding / gridWidth

        val planetArea = area.expand(PlottingConstraints.POINT_SIZE / 2).shrink(
            -min(0.0, max(-planetMinimalPadding, planetSize?.top ?: 0.0)),
            -min(0.0, max(-planetMinimalPadding, planetSize?.left ?: 0.0))
        )
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
            val leftOffset = (paperArea.width - planetArea.width) / 2 - planetMinimalPadding
            val topOffset = (paperArea.height - planetArea.height) / 2 - planetMinimalPadding

            paperArea = paperArea.copy(
                left = paperArea.left - min(leftOffset, max(-leftOffset, offset.left)),
                top = paperArea.top - min(topOffset, max(-topOffset, offset.top))
            )
        }

        backgroundView.setRectangle(paperArea)

        for ((view, edge) in edgeList) {
            view.setRectangle(edge.getArea(paperArea, 0.2))
            view.setColor(
                ViewColor.PRIMARY_BACKGROUND_COLOR.interpolate(
                    ViewColor.LINE_COLOR,
                    if (selectedEdge == edge) 0.5 else 0.1
                )
            )
        }

        measuringView.clear()
        if (orientation == Orientation.HORIZONTAL) {
            var line = paperArea.bottom - planetPaperWidth

            // Draw paper sheet separator
            while (line > paperArea.top) {
                measuringView += LineView(
                    listOf(
                        Point(paperArea.left, line),
                        Point(paperArea.right, line)
                    ),
                    PlottingConstraints.LINE_WIDTH,
                    ViewColor.LINE_COLOR.interpolate(ViewColor.PRIMARY_BACKGROUND_COLOR, 0.5)
                )
                line -= planetPaperWidth
            }

            // Draw paper sheet width marker
            measuringView += MeasuringLineView(
                Point(paperArea.left - SECOND_MEASURING_LINE_PAPER_DISTANCE, paperArea.bottom - planetPaperWidth),
                Point(paperArea.left - SECOND_MEASURING_LINE_PAPER_DISTANCE, paperArea.bottom)
            )
        } else {
            var line = paperArea.left + planetPaperWidth

            // Draw paper sheet separator
            while (line < paperArea.right) {
                measuringView += LineView(
                    listOf(
                        Point(line, paperArea.bottom),
                        Point(line, paperArea.top)
                    ),
                    PlottingConstraints.LINE_WIDTH,
                    ViewColor.LINE_COLOR.interpolate(ViewColor.PRIMARY_BACKGROUND_COLOR, 0.5)
                )
                line += planetPaperWidth
            }

            // Draw paper sheet width marker
            measuringView += MeasuringLineView(
                Point(paperArea.left, paperArea.bottom + SECOND_MEASURING_LINE_PAPER_DISTANCE),
                Point(paperArea.left + planetPaperWidth, paperArea.bottom + SECOND_MEASURING_LINE_PAPER_DISTANCE)
            )
        }

        // Draw planet outline
        measuringView += LineView(
            listOf(
                planetArea.topRight,
                planetArea.topLeft,
                planetArea.bottomLeft,
                planetArea.bottomRight,
                planetArea.topRight
            ),
            PlottingConstraints.LINE_WIDTH,
            ViewColor.LINE_COLOR.interpolate(ViewColor.PRIMARY_BACKGROUND_COLOR, 0.5)
        )
        measuringView += LineView(
            listOf(
                paperArea.topRight, paperArea.topLeft, paperArea.bottomLeft, paperArea.bottomRight, paperArea.topRight
            ),
            PlottingConstraints.LINE_WIDTH,
            ViewColor.LINE_COLOR.interpolate(ViewColor.PRIMARY_BACKGROUND_COLOR, 0.5)
        )

        // Draw paper height marker
        measuringView += MeasuringLineView(
            Point(paperArea.left - FIRST_MEASURING_LINE_PAPER_DISTANCE, paperArea.top),
            Point(paperArea.left - FIRST_MEASURING_LINE_PAPER_DISTANCE, paperArea.bottom)
        )
        // Draw paper width marker
        measuringView += MeasuringLineView(
            Point(paperArea.left, paperArea.bottom + FIRST_MEASURING_LINE_PAPER_DISTANCE),
            Point(paperArea.right, paperArea.bottom + FIRST_MEASURING_LINE_PAPER_DISTANCE)
        )

        // Draw paper - planet vertical padding markers
        val bottomPadding = paperArea.bottom - planetArea.bottom
        val topPadding = planetArea.top - paperArea.top
        measuringView += MeasuringLineView(
            Point(planetArea.right + MEASURING_LINE_PLANET_DISTANCE, paperArea.bottom),
            Point(planetArea.right + MEASURING_LINE_PLANET_DISTANCE, paperArea.bottom - bottomPadding)
        )
        measuringView += MeasuringLineView(
            Point(planetArea.right + MEASURING_LINE_PLANET_DISTANCE, paperArea.top + topPadding),
            Point(planetArea.right + MEASURING_LINE_PLANET_DISTANCE, paperArea.top)
        )
        measuringView += MeasuringLineView(
            Point(planetArea.left - MEASURING_LINE_PLANET_DISTANCE, paperArea.bottom),
            Point(planetArea.left - MEASURING_LINE_PLANET_DISTANCE, paperArea.bottom - bottomPadding)
        )
        measuringView += MeasuringLineView(
            Point(planetArea.left - MEASURING_LINE_PLANET_DISTANCE, paperArea.top + topPadding),
            Point(planetArea.left - MEASURING_LINE_PLANET_DISTANCE, paperArea.top)
        )


        val bottomPlanetPoint = floor(planetArea.bottom).toInt()
        val topPlanetPoint = ceil(planetArea.top).toInt()
        measuringView += MeasuringLineView(
            Point(paperArea.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, paperArea.bottom),
            Point(paperArea.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, bottomPlanetPoint)
        )
        for (y in (topPlanetPoint until bottomPlanetPoint).reversed()) {
            measuringView += MeasuringLineView(
                Point(paperArea.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, y + 1),
                Point(paperArea.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, y)
            )
        }
        measuringView += MeasuringLineView(
            Point(paperArea.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, topPlanetPoint),
            Point(paperArea.right + FIRST_MEASURING_LINE_PAPER_DISTANCE, paperArea.top)
        )

        // Draw paper - planet horizontal padding markers
        val rightPadding = paperArea.right - planetArea.right
        val leftPadding = planetArea.left - paperArea.left
        measuringView += MeasuringLineView(
            Point(paperArea.right, planetArea.top - MEASURING_LINE_PLANET_DISTANCE),
            Point(paperArea.right - rightPadding, planetArea.top - MEASURING_LINE_PLANET_DISTANCE)
        )
        measuringView += MeasuringLineView(
            Point(paperArea.left + leftPadding, planetArea.top - MEASURING_LINE_PLANET_DISTANCE),
            Point(paperArea.left, planetArea.top - MEASURING_LINE_PLANET_DISTANCE)
        )
        measuringView += MeasuringLineView(
            Point(paperArea.right, planetArea.bottom + MEASURING_LINE_PLANET_DISTANCE),
            Point(paperArea.right - rightPadding, planetArea.bottom + MEASURING_LINE_PLANET_DISTANCE)
        )
        measuringView += MeasuringLineView(
            Point(paperArea.left + leftPadding, planetArea.bottom + MEASURING_LINE_PLANET_DISTANCE),
            Point(paperArea.left, planetArea.bottom + MEASURING_LINE_PLANET_DISTANCE)
        )

        val rightPlanetPoint = floor(planetArea.right).toInt()
        val leftPlanetPoint = ceil(planetArea.left).toInt()
        measuringView += MeasuringLineView(
            Point(paperArea.right, paperArea.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
            Point(rightPlanetPoint, paperArea.top - FIRST_MEASURING_LINE_PAPER_DISTANCE)
        )
        for (x in (leftPlanetPoint until rightPlanetPoint).reversed()) {
            measuringView += MeasuringLineView(
                Point(x + 1, paperArea.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
                Point(x, paperArea.top - FIRST_MEASURING_LINE_PAPER_DISTANCE)
            )
        }
        measuringView += MeasuringLineView(
            Point(leftPlanetPoint, paperArea.top - FIRST_MEASURING_LINE_PAPER_DISTANCE),
            Point(paperArea.left, paperArea.top - FIRST_MEASURING_LINE_PAPER_DISTANCE)
        )
    }

    fun importPlanet(planet: Planet) {
        val newArea = AbsPlanetDrawable.calcPlanetArea(planet) ?: Rectangle.ZERO
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

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }

    companion object {
        private const val FIRST_MEASURING_LINE_PAPER_DISTANCE: Double = 0.1
        private const val SECOND_MEASURING_LINE_PAPER_DISTANCE: Double =
            FIRST_MEASURING_LINE_PAPER_DISTANCE + MeasuringLineView.MEASURING_LINE_ENDING_WIDTH / 2

        private const val MEASURING_LINE_PLANET_DISTANCE: Double = -MeasuringLineView.MEASURING_LINE_ENDING_WIDTH / 2
    }
}
