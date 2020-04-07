package de.robolab.renderer.drawable.planet

import de.robolab.model.Path
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.edit.*
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.utils.Pointer
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class EditPlanetDrawable() : AbsPlanetDrawable() {

    var editCallback: IEditCallback = object : IEditCallback {}

    val pointer: Pointer
        get() = plotter?.pointer ?: Pointer()

    private val editPointDrawable = EditPointDrawable(this)
    private val editPathDrawable = EditDrawPathDrawable(this)
    private val editDrawEndDrawable = EditDrawEndDrawable(this)
    private val editControlPointsDrawable = EditControlPointsDrawable(this)
    private val editPathSelectDrawable = EditPathSelectDrawable(this)
    private val editMenuDrawable = EditMenuDrawable(this)

    val editableProperty = property(false)
    val editable by editableProperty

    val menuProperty = property<Menu?>(null)
    var menu by menuProperty

    fun menu(name: String, init: MenuBuilder.() -> Unit) {
        menu = menu(pointer.position, name, init)
    }

    private val selectedPathControlPointsProperty = selectedElementsProperty.mapBinding {
        val path = selectedElement<Path>() ?: return@mapBinding null

        PathAnimatable.getControlPointsFromPath(path)
    }
    val selectedPathControlPoints by selectedPathControlPointsProperty
    var selectedPointEnd: EditDrawEndDrawable.PointEnd? = null

    private val planetLayer = PlanetLayer(this)

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)

        editPointDrawable.importPlanet(planet)
        editDrawEndDrawable.importPlanet(planet)
        editPathSelectDrawable.importPlanet(planet)

        importPlanets()
    }

    init {
        editableProperty.onChange {
            if (editable) {
                editPointDrawable.startEnterAnimation { }
            } else {
                editPointDrawable.startExitAnimation { }
            }
        }


        buildDrawableList(
                planetLayers = listOf(
                        planetLayer
                ),
                overlays = listOf(
                        editPointDrawable,
                        editDrawEndDrawable,
                        editPathSelectDrawable,
                        editPathDrawable,
                        editControlPointsDrawable,
                        editMenuDrawable
                )
        )
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        if (super.onKeyPress(event)) return true
        if (!editable) return false

        when (event.keyCode) {
            KeyCode.UNDO -> {
                editCallback.undo()
            }
            KeyCode.REDO -> {
                editCallback.redo()
            }
            KeyCode.Z -> if (event.ctrlKey) {
                if (event.shiftKey) {
                    editCallback.redo()
                } else {
                    editCallback.undo()
                }
            }
            else -> {
                return false
            }
        }
        return true
    }
}
