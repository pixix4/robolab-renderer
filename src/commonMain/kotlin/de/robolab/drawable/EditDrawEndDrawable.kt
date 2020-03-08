package de.robolab.drawable

import de.robolab.model.Direction
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

class EditDrawEndDrawable(
        private val editPlanet: EditPlanetDrawable
) : IDrawable {

    data class PointEnd(
            val point: Pair<Int, Int>,
            val direction: Direction
    )

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    private fun drawPointEnd(context: DrawContext, pointEnd: PointEnd) {
        val p0 = Point(pointEnd.point.first, pointEnd.point.second)

        if (editPlanet.selectedPathControlPoints?.any { it.distance(editPlanet.pointer.position) < PlottingConstraints.POINT_SIZE / 2 } == true) {
            return
        }

        val d = when (pointEnd.direction) {
            Direction.NORTH -> Point(0.0, 1.0)
            Direction.EAST -> Point(1.0, 0.0)
            Direction.SOUTH -> Point(0.0, -1.0)
            Direction.WEST -> Point(-1.0, 0.0)
        }

        val p1 = p0 + d * (PlottingConstraints.TARGET_RADIUS - PlottingConstraints.LINE_WIDTH * 2)
        val p2 = p1 + d * PlottingConstraints.LINE_WIDTH

        context.strokeLine(listOf(p0, p1), context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
        context.strokeArc(p2, PlottingConstraints.LINE_WIDTH, 0.0, 2 * PI, context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
    }

    override fun onDraw(context: DrawContext) {
        val pointEnd = editPlanet.pointer.findObjectUnderPointer<PointEnd>()
        if (pointEnd != null) {
            drawPointEnd(context, pointEnd)
        }

        val startPointEnd = editPlanet.interaction.startEnd
        if (startPointEnd != null) {
            drawPointEnd(context, startPointEnd)
        }
    }

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val col = (position.left).roundToInt()
        val dx = abs(position.left - col)
        val row = (position.top).roundToInt()
        val dy = abs(position.top - row)

        if (
                (dx < PlottingConstraints.POINT_SIZE && dy < PlottingConstraints.TARGET_RADIUS) ||
                (dx < PlottingConstraints.TARGET_RADIUS && dy < PlottingConstraints.POINT_SIZE)
        ) {
            val direction = when {
                position.left - col > PlottingConstraints.POINT_SIZE / 2 -> Direction.EAST
                col - position.left > PlottingConstraints.POINT_SIZE / 2 -> Direction.WEST
                position.top - row > PlottingConstraints.POINT_SIZE / 2 -> Direction.NORTH
                row - position.top > PlottingConstraints.POINT_SIZE / 2 -> Direction.SOUTH
                else -> return emptyList()
            }

            val point = col to row

            if (planet.pathList.any {
                        it.source == point && it.sourceDirection == direction ||
                                it.target == point && it.targetDirection == direction
                    }) {
                return emptyList()
            }

            return listOf(PointEnd(point, direction))
        }

        return emptyList()
    }
}
