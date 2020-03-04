package de.robolab.renderer.interaction

import de.robolab.drawable.EditPlanetDrawable
import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.Transformation
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.platform.MouseEvent
import kotlin.math.abs
import kotlin.math.roundToInt

class EditPlanetInteraction(
        private val transformation: Transformation,
        private val editPlanet: EditPlanetDrawable
) : ICanvasListener {

    data class PointEnd(
            val left: Int,
            val top: Int,
            val direction: Direction
    )

    private fun pointEndFromEvent(event: MouseEvent): PointEnd? {
        val point = transformation.canvasToPlanet(event.point)
        val col = (point.left).roundToInt()
        val dx = abs(point.left - col)
        val row = (point.top).roundToInt()
        val dy = abs(point.top - row)

        // clicked on direction stub of point (for path editing)
        if (
                (dx < PlottingConstraints.POINT_SIZE && dy < PlottingConstraints.TARGET_RADIUS) ||
                (dx < PlottingConstraints.TARGET_RADIUS && dy < PlottingConstraints.POINT_SIZE)
        ) {
            val direction = when {
                point.left - col > PlottingConstraints.POINT_SIZE / 2 -> Direction.EAST
                col - point.left > PlottingConstraints.POINT_SIZE / 2 -> Direction.WEST
                point.top - row > PlottingConstraints.POINT_SIZE / 2 -> Direction.NORTH
                row - point.top > PlottingConstraints.POINT_SIZE / 2 -> Direction.SOUTH
                else -> return null
            }

            return PointEnd(col, row, direction)
        }

        return null
    }

    var startEnd: PointEnd? = null
    var targetEnd: PointEnd? = null
    private var hasMoved = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        if (startEnd == null) {
            startEnd = pointEndFromEvent(event)
            if (startEnd != null) {
                targetEnd = null
                hasMoved = false

                return true
            }
        }

        return false
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        if (startEnd != null && hasMoved) {
            targetEnd = pointEndFromEvent(event)

            startEnd?.let { (startX, startY, startDirection) ->
                targetEnd?.let { (endX, endY, endDirection) ->
                    editPlanet.editCallback.onDrawPath(
                            startX to startY,
                            startDirection,
                            endX to endY,
                            endDirection
                    )
                }
            }

            startEnd = null
            targetEnd = null
            hasMoved = false
            return true
        }

        if (!hasMoved) {
            hasMoved = true
        }

        return false
    }

    override fun onMouseMove(event: MouseEvent): Boolean {
        return onMouseDrag(event)
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        hasMoved = true

        if (startEnd != null) {
            targetEnd = pointEndFromEvent(event)

            return false
        }

        return false
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        editPlanet.selectedPath = editPlanet.pointer.objectUnderPointer as? Path?

        return false
    }
}
