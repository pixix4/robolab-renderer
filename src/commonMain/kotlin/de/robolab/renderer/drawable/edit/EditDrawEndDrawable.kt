package de.robolab.renderer.drawable.edit

import de.robolab.model.Coordinate
import de.robolab.model.Direction
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

class EditDrawEndDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    data class PointEnd(
            val point: Coordinate,
            val direction: Direction
    )

    private val pointEndsToDraw = mutableListOf<PointEnd>()

    override fun onUpdate(ms_offset: Double): Boolean {
        var changes = false
        if (editPlanetDrawable.selectedPathControlPoints?.any { it.distance(editPlanetDrawable.pointer.position) < PlottingConstraints.POINT_SIZE / 2 } == true) {
            if (pointEndsToDraw.isNotEmpty()) {
                pointEndsToDraw.clear()
                changes = true
            }
        } else {
            val old = pointEndsToDraw.hashCode()
            pointEndsToDraw.clear()
            val pointEnd = editPlanetDrawable.pointer.findObjectUnderPointer<PointEnd>()
            if (pointEnd != null) {
                pointEndsToDraw += pointEnd
            }

            val startPointEnd = editPlanetDrawable.selectedPointEnd
            if (startPointEnd != null) {
                pointEndsToDraw += startPointEnd
            }

            changes = pointEndsToDraw.hashCode() != old
        }

        return changes
    }

    private fun drawPointEnd(context: DrawContext, pointEnd: PointEnd) {
        val p = Point(pointEnd.point.x, pointEnd.point.y)

        val d = when (pointEnd.direction) {
            Direction.NORTH -> Point(0.0, 1.0)
            Direction.EAST -> Point(1.0, 0.0)
            Direction.SOUTH -> Point(0.0, -1.0)
            Direction.WEST -> Point(-1.0, 0.0)
        }

        val p0 = p + d * (PlottingConstraints.POINT_SIZE / 2)
        val p1 = p + d * (PlottingConstraints.TARGET_RADIUS - PlottingConstraints.LINE_WIDTH * 2)
        val p2 = p1 + d * PlottingConstraints.LINE_WIDTH

        context.strokeLine(listOf(p0, p1), context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
        context.strokeArc(p2, PlottingConstraints.LINE_WIDTH, 0.0, 2 * PI, context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
    }

    override fun onDraw(context: DrawContext) {
        for (p in pointEndsToDraw) {
            drawPointEnd(context, p)
        }
    }

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        if (editPlanetDrawable.selectedElement<Coordinate>() != null) {
            return emptyList()
        }

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

            return listOf(PointEnd(Coordinate(col, row), direction))
        }

        return emptyList()
    }

    private var allowClickCreate = false
    override fun onPointerDown(event: PointerEvent): Boolean {
        allowClickCreate = false
        val currentPointEnd = editPlanetDrawable.pointer.findObjectUnderPointer<PointEnd>()

        if (currentPointEnd == null) {
            editPlanetDrawable.selectedPointEnd = null
            return false
        }

        if (editPlanetDrawable.selectedPointEnd != null) {
            allowClickCreate = true
            return true
        }

        val path = planet.pathList.find {
            it.source == currentPointEnd.point && it.sourceDirection == currentPointEnd.direction ||
                    it.target == currentPointEnd.point && it.targetDirection == currentPointEnd.direction
        }

        if (path != null) {
            if (path.source == currentPointEnd.point && path.sourceDirection == currentPointEnd.direction) {
                editPlanetDrawable.selectedPointEnd = PointEnd(path.target, path.targetDirection)
            } else {
                editPlanetDrawable.selectedPointEnd = PointEnd(path.source, path.sourceDirection)
            }
        } else {
            editPlanetDrawable.selectedPointEnd = currentPointEnd
        }

        return true
    }

    override fun onPointerDrag(event: PointerEvent): Boolean {
        return editPlanetDrawable.selectedPointEnd != null
    }

    override fun onPointerUp(event: PointerEvent): Boolean {
        if (!editPlanetDrawable.editable) return false

        if (editPlanetDrawable.selectedPointEnd != null && (event.hasMoved || allowClickCreate)) {
            val sourceEnd = editPlanetDrawable.selectedPointEnd ?: return false
            editPlanetDrawable.selectedPointEnd = null
            val targetEnd = editPlanetDrawable.pointer.findObjectUnderPointer<PointEnd>() ?: return false

            var groupHistory = false

            for (end in sequenceOf(sourceEnd, targetEnd)) {
                val path = planet.pathList.find {
                    it.source == end.point && it.sourceDirection == end.direction ||
                            it.target == end.point && it.targetDirection == end.direction
                } ?: continue

                editPlanetDrawable.editCallback.deletePath(path, groupHistory)
                groupHistory = true
            }

            editPlanetDrawable.editCallback.createPath(
                    sourceEnd.point,
                    sourceEnd.direction,
                    targetEnd.point,
                    targetEnd.direction,
                    groupHistory
            )

            return true
        }

        return false
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyCode.ESCAPE -> {
                editPlanetDrawable.selectedPointEnd = null
            }
            else -> {
                return false
            }
        }
        return false
    }
}
