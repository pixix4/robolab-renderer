package de.robolab.drawable

import de.robolab.model.Direction
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.drawable.IRootDrawable
import de.robolab.renderer.interaction.EditPlanetInteraction

class EditPlanetDrawable() : IRootDrawable, IAnimationTime {

    lateinit var plotter: DefaultPlotter

    override val animationTime: Double
        get() = plotter.animationTime

    var callback: EditPlanetInteraction.ICallback = object: EditPlanetInteraction.ICallback {
        override fun onDrawPath(startPoint: Pair<Int, Int>, startDirection: Direction, endPoint: Pair<Int, Int>, endDirection: Direction) {
        }
    }

    private val planetBackground = BackgroundDrawable(this)

    private val pointDrawable = PointDrawable(this)
    private val pathDrawable = PathListDrawable(this)
    private val targetDrawable = TargetDrawable(this)
    private val senderDrawable = SenderDrawable(this)
    private val pathSelectDrawable = PathSelectDrawable(this)
    private val editPointDrawable = EditPointDrawable(this)
    private val editPathDrawable = EditPathDrawable(this)


    lateinit var interaction: EditPlanetInteraction

    private val planetForeground = GroupDrawable(
            editPointDrawable,
            targetDrawable,
            senderDrawable,
            pathDrawable,
            pathSelectDrawable,
            pointDrawable,
            editPathDrawable
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
        val shouldStartAnimation = !this::plotter.isInitialized
        this.plotter = plotter

        if (shouldStartAnimation) editPointDrawable.startEnterAnimation { }

        interaction = EditPlanetInteraction(plotter.transformation, callback)
        plotter.pushInteraction(interaction)
    }

    override fun onDetach(plotter: DefaultPlotter) {
        plotter.popInteraction()
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
        editPointDrawable.importPlanet(planet)
    }
}
