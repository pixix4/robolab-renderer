package de.robolab.renderer.drawable

import de.robolab.planet.Planet
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.DrawContext

class NameDrawable() : IDrawable {

    private var name = ""
    private var changed = false

    override fun onUpdate(ms_offset: Double): Boolean {
        if (changed) {
            changed = false
            return true
        }

        return false
    }

    override fun onDraw(context: DrawContext) {
        val center = Point(context.width - CompassDrawable.RIGHT_PADDING, CompassDrawable.TOP_PADDING)
        context.canvas.fillText(
                name,
                center,
                context.theme.plotter.lineColor.a(0.75),
                40.0,
                ICanvas.FontAlignment.RIGHT,
                ICanvas.FontWeight.BOLD
        )
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    fun importPlanet(planet: Planet) {
        if (planet.name != name) {
            name = planet.name
            changed = true
        }
    }
}
