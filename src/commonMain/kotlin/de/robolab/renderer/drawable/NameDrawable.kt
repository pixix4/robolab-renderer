package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.utils.BSpline
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.Transformation
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class NameDrawable(
        private val planetDrawable: PlanetDrawable
) : IDrawable {

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
        if (!planetDrawable.drawName) return

        val center = Point(context.width - CompassDrawable.RIGHT_PADDING, CompassDrawable.TOP_PADDING)
        context.canvas.fillText(
                name,
                center,
                context.theme.lineColor.a(0.75),
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
