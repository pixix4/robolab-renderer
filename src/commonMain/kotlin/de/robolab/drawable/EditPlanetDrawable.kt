package de.robolab.drawable

import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.Pointer
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.interaction.EditPlanetInteraction
import de.westermann.kobserve.property.property

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

    lateinit var interaction: EditPlanetInteraction
    
    val selectedPathProperty = property<Path?>(null)
    var selectedPath by selectedPathProperty

    override val drawable = GroupDrawable(
            planetBackground,
            viewBackground,
            editPointDrawable,
            planetForeground,
            editPathDrawable,
            viewForeground
    )

    override fun onAttach(plotter: DefaultPlotter) {
        val shouldStartAnimation = this.plotter == null
        super.onAttach(plotter)

        if (shouldStartAnimation) editPointDrawable.startEnterAnimation { }

        interaction = EditPlanetInteraction(plotter.transformation, this)
        plotter.pushInteraction(interaction)
    }

    override fun onDetach(plotter: DefaultPlotter) {
        plotter.popInteraction()
        super.onDetach(plotter)
    }

    override fun importPlanet(planet: Planet) {
        super.importPlanet(planet)
        editPointDrawable.importPlanet(planet)
    }
}
