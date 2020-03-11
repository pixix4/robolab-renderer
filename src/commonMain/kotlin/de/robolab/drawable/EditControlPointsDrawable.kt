package de.robolab.drawable

import de.robolab.model.Path
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable
import kotlin.math.PI
import kotlin.math.roundToInt

class EditControlPointsDrawable(
        private val editPlanet: EditPlanetDrawable
) : IDrawable {

    data class ControlPoint(
            val path: Path,
            val point: Int
    )

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        val controlPoints = editPlanet.selectedPathControlPoints ?: return
        
        val first = controlPoints.first().let {
            Point(it.left.roundToInt(), it.top.roundToInt())
        }.interpolate(controlPoints.first(), 1.0 - (PlottingConstraints.POINT_SIZE / 2) / PlottingConstraints.CURVE_SECOND_POINT)
        val last = controlPoints.last().let {
            Point(it.left.roundToInt(), it.top.roundToInt())
        }.interpolate(controlPoints.last(), 1.0 - (PlottingConstraints.POINT_SIZE / 2) / PlottingConstraints.CURVE_SECOND_POINT)

        context.strokeLine(listOf(first) + controlPoints + last, context.theme.editColor, PlottingConstraints.LINE_WIDTH / 2)

        for ((i, point) in controlPoints.withIndex()) {
            if (i == 0 || i == controlPoints.size - 1) {
                continue
            }

            val divider = if (editPlanet.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2) 2 else 4

            context.fillArc(point, PlottingConstraints.POINT_SIZE / divider, 0.0, 2.0 * PI, context.theme.editColor)
            context.fillText(i.toString(), point, context.theme.primaryBackgroundColor, 4.0)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val path = editPlanet.selectedPath ?: return emptyList()
        val controlPoints = editPlanet.selectedPathControlPoints ?: return emptyList()

        for ((i, point) in controlPoints.withIndex()) {
            if (i == 0 || i == controlPoints.size - 1) {
                continue
            }

            if (editPlanet.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2) {
                return listOf(ControlPoint(
                        path, i
                ))
            }
        }

        return emptyList()
    }
}
