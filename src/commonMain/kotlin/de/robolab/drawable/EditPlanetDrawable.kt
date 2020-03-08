package de.robolab.drawable

import de.robolab.drawable.utils.PathGenerator
import de.robolab.model.Direction
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.Pointer
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.interaction.EditPlanetInteraction
import de.westermann.kobserve.property.mapBinding

class EditPlanetDrawable() : PlanetDrawable() {

    interface IEditCallback {
        fun onDrawPath(startPoint: Pair<Int, Int>, startDirection: Direction, endPoint: Pair<Int, Int>, endDirection: Direction)
    }

    var editCallback: IEditCallback = object : IEditCallback {
        override fun onDrawPath(startPoint: Pair<Int, Int>, startDirection: Direction, endPoint: Pair<Int, Int>, endDirection: Direction) {
        }
    }

    val pointer: Pointer
        get() = plotter?.pointer ?: Pointer()

    private val editPointDrawable = EditPointDrawable(this)
    private val editPathDrawable = EditDrawPathDrawable(this)
    private val editDrawEndDrawable = EditDrawEndDrawable(this)
    private val editControlPointsDrawable = EditControlPointsDrawable(this)

    lateinit var interaction: EditPlanetInteraction

    val selectedPathControlPointsProperty = selectedPathProperty.mapBinding { nullablePath ->
        val path = nullablePath ?: return@mapBinding null

        val startPoint = Point(path.source.first, path.source.second)
        val startDirection = path.sourceDirection
        val endPoint = Point(path.target.first, path.target.second)
        val endDirection = path.targetDirection

        val linePoints = null ?: PathGenerator.generateControlPoints(
                startPoint,
                startDirection,
                endPoint,
                endDirection
        )

        PathDrawable.linePointsToControlPoints(
                linePoints,
                startPoint,
                startDirection,
                endPoint,
                endDirection
        )
    }
    val selectedPathControlPoints by selectedPathControlPointsProperty

    override val drawable = GroupDrawable(
            planetBackground,
            viewBackground,
            editPointDrawable,
            planetForeground,
            editDrawEndDrawable,
            editPathDrawable,
            editControlPointsDrawable,
            viewForeground
    )

    override fun onAttach(plotter: DefaultPlotter) {
        val shouldStartAnimation = this.plotter == null
        super.onAttach(plotter)

        if (shouldStartAnimation) editPointDrawable.startEnterAnimation { }

        interaction = EditPlanetInteraction(this)
        plotter.pushInteraction(interaction)
    }

    override fun onDetach(plotter: DefaultPlotter) {
        plotter.popInteraction()
        super.onDetach(plotter)
    }

    override fun importPlanet(planet: Planet) {
        super.importPlanet(planet)
        editPointDrawable.importPlanet(planet)
        editDrawEndDrawable.importPlanet(planet)
    }
}
