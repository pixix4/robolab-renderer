package de.robolab.renderer.drawable.live

import de.robolab.model.Planet
import de.robolab.renderer.IPlotter
import de.robolab.renderer.drawable.BackgroundDrawable
import de.robolab.renderer.drawable.PlanetDrawable
import de.robolab.renderer.drawable.base.IPlanetDrawable
import de.robolab.renderer.utils.DrawContext

class LivePlanetDrawable() : IPlanetDrawable() {

    private val backgroundPlanetDrawable = PlanetDrawable()
    private val foregroundPlanetDrawable = PlanetDrawable()

    private val robotDrawable = RobotDrawable(this)
    private val backgroundDrawable = BackgroundDrawable(this)

    var plotter: IPlotter? = null

    override val animationTime: Double
        get() = plotter?.animationTime ?: 0.0

    override val drawableList = listOf(
            backgroundDrawable,
            foregroundPlanetDrawable,
            robotDrawable
    )

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = super.onUpdate(ms_offset)

        if (backgroundPlanetDrawable.onUpdate(ms_offset)) {
            hasChanges = true
        }

        return hasChanges
    }

    override fun onDraw(context: DrawContext) {
        backgroundDrawable.onDraw(context)

        foregroundPlanetDrawable.viewBackground.onDraw(context)

        backgroundPlanetDrawable.planetForeground.onDraw(context.withAlpha(0.2))

        foregroundPlanetDrawable.planetForeground.onDraw(context)
        foregroundPlanetDrawable.viewForeground.onDraw(context)

        robotDrawable.onDraw(context)
    }

    private var backgroundPlanet: Planet? = null
    fun importBackgroundPlanet(planet: Planet) {
        backgroundPlanet = planet

        backgroundPlanetDrawable.importPlanet(planet)
        backgroundDrawable.importPlanet(listOfNotNull(backgroundPlanet, foregroundPlanet))
    }

    private var foregroundPlanet: Planet? = null
    fun importForegroundPlanet(planet: Planet, robot: RobotDrawable.Robot?) {
        foregroundPlanet = planet

        val background = backgroundPlanet
        if (background != null) {
            foregroundPlanet = importPlanetSplines(planet, background)
        }

        foregroundPlanetDrawable.importPlanet(planet)
        backgroundDrawable.importPlanet(listOfNotNull(backgroundPlanet, foregroundPlanet))
        robotDrawable.importRobot(backgroundPlanet, robot)
    }

    override fun onAttach(plotter: IPlotter) {
        super.onAttach(plotter)
        this.plotter = plotter
    }

    override fun onDetach(plotter: IPlotter) {
        super.onDetach(plotter)
        this.plotter = null
    }

    private fun importPlanetSplines(planet: Planet, background: Planet): Planet {
        var tmp = planet

        tmp = tmp.copy(
                name = background.name,
                bluePoint = background.bluePoint
        )

        if (background.startPoint != null && background.startPoint.point == tmp.startPoint?.point && background.startPoint.orientation == tmp.startPoint?.orientation) {
            tmp = tmp.copy(
                    startPoint = tmp.startPoint?.copy(controlPoints = background.startPoint.controlPoints)
            )
        }

        tmp = tmp.copy(
                pathList = tmp.pathList.map { path ->
                    val backgroundPath = background.pathList.find { it.equalPath(path) } ?: return@map path

                    path.copy(
                            controlPoints = backgroundPath.controlPoints
                    )
                }
        )

        return tmp
    }
}
