package de.robolab.drawable

import de.robolab.model.Direction
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable
import de.robolab.renderer.interaction.EditPlanetInteraction
import kotlin.math.PI

class EditDrawEndDrawable(
        private val plotter: EditPlanetDrawable
) : IDrawable {

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    private fun drawPointEnd(pointEnd: EditPlanetInteraction.PointEnd?, context: DrawContext) {

        val (left, top, direction) = pointEnd ?: return
        val p0 = Point(left, top)

        val d = when (direction) {
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
        drawPointEnd(plotter.interaction.startEnd, context)
        drawPointEnd(plotter.interaction.targetEnd, context)
        drawPointEnd(plotter.interaction.currentPointEnd, context)
    }

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        return null
    }
}









