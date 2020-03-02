package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.drawable.IDrawable

class PlanetDrawable(
        private val plotter: DefaultPlotter
) : IDrawable {

    private val planetBackground = BackgroundDrawable(plotter)

    private val pointDrawable = PointDrawable(plotter)
    private val pathDrawable = PathListDrawable(plotter)
    private val targetDrawable = TargetDrawable(plotter)
    private val senderDrawable = SenderDrawable(plotter)
    private val pathSelectDrawable = PathSelectDrawable(plotter)
    private val editPointDrawable = EditPointDrawable(plotter)

    private val planetForeground = GroupDrawable(
            editPointDrawable,
            targetDrawable,
            senderDrawable,
            pathDrawable,
            pathSelectDrawable,
            pointDrawable
    )

    private val viewBackground = GroupDrawable(
            GridLinesDrawable
    )

    private val viewForeground = GroupDrawable(
            GridNumbersDrawable,
            CompassDrawable
    )

    private val drawable = GroupDrawable(
            planetBackground,
            viewBackground,
            planetForeground,
            viewForeground
    )

    override fun onUpdate(ms_offset: Double): Boolean {
        return drawable.onUpdate(ms_offset)
    }

    override fun onDraw(context: DrawContext) {
        drawable.onDraw(context)
    }

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        return drawable.getObjectAtPosition(context, position)
    }
    
    fun importPlanet(planet: Planet) {
        planetBackground.importPlanet(planet)
        pointDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)
        editPointDrawable.importPlanet(planet)
    }

    init {
        editPointDrawable.startEnterAnimation {}
    }
}
