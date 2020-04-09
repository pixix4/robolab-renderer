package de.robolab.renderer.drawable.edit

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.general.PathSelectAnimatableManager
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
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

        val position = editPlanetDrawable.pointer?.position
        pathSelectToDraw = if (position != null && editPlanetDrawable.selectedPathControlPoints?.any { it.distance(position) < PlottingConstraints.POINT_SIZE / 2 } == true) {
            null
        } else {
            editPlanetDrawable.pointer?.findObjectUnderPointer()
        }

        return pathSelectToDraw != oldPathSelectToDraw
    }

    override fun onDraw(context: DrawContext) {
        val d = pathSelectToDraw ?: return
        context.fillPolygon(PathSelectAnimatableManager.getArrow(Point(d.point), d.direction), context.theme.gridTextColor)
    }

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        val selectedPoint = editPlanetDrawable.selectedElement<Coordinate>() ?: return emptyList()

        val dx = abs(position.left - selectedPoint.x)
        val dy = abs(position.top - selectedPoint.y)

        if (
                (dx < PlottingConstraints.POINT_SIZE && dy < PlottingConstraints.TARGET_RADIUS) ||
                (dx < PlottingConstraints.TARGET_RADIUS && dy < PlottingConstraints.POINT_SIZE)
        ) {
            val direction = when {
                position.left - selectedPoint.x > PlottingConstraints.POINT_SIZE / 2 -> Direction.EAST
                selectedPoint.x - position.left > PlottingConstraints.POINT_SIZE / 2 -> Direction.WEST
                position.top - selectedPoint.y > PlottingConstraints.POINT_SIZE / 2 -> Direction.NORTH
                selectedPoint.y - position.top > PlottingConstraints.POINT_SIZE / 2 -> Direction.SOUTH
                else -> return emptyList()
            }

            return listOf(PointSelect(selectedPoint, direction))
        }

        return emptyList()
    }

    override fun onPointerDown(event: PointerEvent, position: Point): Boolean {
        if (!editPlanetDrawable.editable) return false

        return editPlanetDrawable.pointer?.findObjectUnderPointer<PointSelect>() != null
    }

    override fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        if (!editPlanetDrawable.editable || event.hasMoved) return false

        val currentPathSelect = editPlanetDrawable.pointer?.findObjectUnderPointer<PointSelect>() ?: return false
        editPlanetDrawable.editCallback.togglePathSelect(currentPathSelect.point, currentPathSelect.direction)
        return true
    }
}
