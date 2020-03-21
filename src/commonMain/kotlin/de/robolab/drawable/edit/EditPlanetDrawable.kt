package de.robolab.drawable.edit

import de.robolab.drawable.PathDrawable
import de.robolab.drawable.PlanetDrawable
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.Pointer
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.interaction.EditPlanetInteraction
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class EditPlanetDrawable() : PlanetDrawable() {

    var editCallback: IEditCallback = object : IEditCallback {}

    val pointer: Pointer
        get() = plotter?.pointer ?: Pointer()

    private val editPointDrawable = EditPointDrawable(this)
    private val editPathDrawable = EditDrawPathDrawable(this)
    private val editDrawEndDrawable = EditDrawEndDrawable(this)
    private val editControlPointsDrawable = EditControlPointsDrawable(this)
    private val editPathSelectDrawable = EditPathSelectDrawable(this)

    val editableProperty = property(false)
    val editable by editableProperty

    lateinit var interaction: EditPlanetInteraction

    private val selectedPathControlPointsProperty = selectedPathProperty.mapBinding { nullablePath ->
        val path = nullablePath ?: return@mapBinding null

        PathDrawable.getControlPointsFromPath(path)
    }
    val selectedPathControlPoints by selectedPathControlPointsProperty

    override val drawable = GroupDrawable(
            planetBackground,
            viewBackground,
            editPointDrawable,
            planetForeground,
            editDrawEndDrawable,
            editPathSelectDrawable,
            editPathDrawable,
            editControlPointsDrawable,
            viewForeground
    )

    override fun onAttach(plotter: DefaultPlotter) {
        super.onAttach(plotter)

        if (!this::interaction.isInitialized) {
            interaction = EditPlanetInteraction(this)
        }
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
        editPathSelectDrawable.importPlanet(planet)
    }

    init {
        editableProperty.onChange {
            if (editable) {
                editPointDrawable.startEnterAnimation { }
            } else {
                editPointDrawable.startExitAnimation { }
            }
        }
    }
}
