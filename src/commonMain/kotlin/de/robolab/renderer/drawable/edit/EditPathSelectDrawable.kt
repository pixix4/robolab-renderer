package de.robolab.renderer.drawable.edit

import de.robolab.model.Coordinate
import de.robolab.model.Direction
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable
import de.robolab.renderer.drawable.PathSelectDrawable
import kotlin.math.abs

class EditPathSelectDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    data class PointSelect(
            val point: Coordinate,
            val direction: Direction
    )

    private var pathSelectToDraw: PointSelect? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        val oldPathSelectToDraw = pathSelectToDraw

        pathSelectToDraw = if (editPlanetDrawable.selectedPathControlPoints?.any { it.distance(editPlanetDrawable.pointer.position) < PlottingConstraints.POINT_SIZE / 2 } == true) {
            null
        } else {
            editPlanetDrawable.pointer.findObjectUnderPointer()
        }

        return pathSelectToDraw != oldPathSelectToDraw
    }

    override fun onDraw(context: DrawContext) {
        val d = pathSelectToDraw ?: return
        context.fillPolygon(PathSelectDrawable.getArrow(Point(d.point), d.direction), context.theme.gridTextColor)
    }

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        val selectedPoint = editPlanetDrawable.selectedPoint ?: return emptyList()

        val dx = abs(position.left - selectedPoint.x)
        val dy = abs(position.top - selectedPoint.y)

        if (
                (dx < PlottingConstraints.POINT_SIZE && dy < PlottingConstraints.TARGET_RADIUS) ||
                (dx < PlottingConstraints.TARGET_RADIUS && dy < PlottingConstraints.POINT_SIZE)
        ) {
            val direction = when {
                position.left - selectedPoint.y > PlottingConstraints.POINT_SIZE / 2 -> Direction.EAST
                selectedPoint.y - position.left > PlottingConstraints.POINT_SIZE / 2 -> Direction.WEST
                position.top - selectedPoint.x > PlottingConstraints.POINT_SIZE / 2 -> Direction.NORTH
                selectedPoint.x - position.top > PlottingConstraints.POINT_SIZE / 2 -> Direction.SOUTH
                else -> return emptyList()
            }

            return listOf(PointSelect(selectedPoint, direction))
        }

        return emptyList()
    }
}
