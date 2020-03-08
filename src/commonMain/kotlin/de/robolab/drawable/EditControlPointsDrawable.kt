package de.robolab.drawable

import de.robolab.model.Path
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable
import kotlin.math.PI

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

        context.strokeLine(controlPoints, context.theme.editColor, PlottingConstraints.LINE_WIDTH / 2)

        for ((i, point) in controlPoints.withIndex()) {
            val divider = if (
                    i > 1 &&
                    i < controlPoints.size - 2 &&
                    editPlanet.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2
            ) 2 else 4
            context.fillArc(point, PlottingConstraints.POINT_SIZE / divider, 0.0, 2.0 * PI, context.theme.editColor)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val path = editPlanet.selectedPath ?: return emptyList()
        val controlPoints = editPlanet.selectedPathControlPoints ?: return emptyList()

        for ((i, point) in controlPoints.withIndex()) {
            if (
                    i > 1 &&
                    i < controlPoints.size - 2 &&
                    editPlanet.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2
            ) {
                return listOf(ControlPoint(
                        path, i
                ))
            }
        }

        return emptyList()
    }

    companion object {
        private const val COLOR_OPACITY = 0.85
    }
}
