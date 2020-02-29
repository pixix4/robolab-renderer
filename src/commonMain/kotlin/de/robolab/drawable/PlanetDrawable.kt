package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.drawable.IDrawable

class PlanetDrawable : IDrawable {

    private val planetBackground = BackgroundDrawable()

    private val pointDrawable = PointDrawable()
    private val pathDrawable = PathListDrawable()
    private val targetDrawable = TargetDrawable()
    private val senderDrawable = SenderDrawable()
    private val pathSelectDrawable = PathSelectDrawable()

    private val planetForeground = GroupDrawable(
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
    
    fun importPlanet(planet: Planet) {
        planetBackground.importPlanet(planet)
        pointDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)
    }
}
