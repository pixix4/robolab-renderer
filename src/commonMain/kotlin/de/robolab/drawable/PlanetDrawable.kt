package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.drawable.IRootDrawable

class PlanetDrawable() : IRootDrawable, IAnimationTime {

    lateinit var plotter: DefaultPlotter

    override val animationTime: Double
        get() = plotter.animationTime

    private val planetBackground = BackgroundDrawable(this)

    private val pointDrawable = PointDrawable(this)
    private val pathDrawable = PathListDrawable(this)
    private val targetDrawable = TargetDrawable(this)
    private val senderDrawable = SenderDrawable(this)
    private val pathSelectDrawable = PathSelectDrawable(this)

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

    override fun onAttach(plotter: DefaultPlotter) {
        this.plotter = plotter
    }

    override fun onDetach(plotter: DefaultPlotter) {
    }

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
    }
}
