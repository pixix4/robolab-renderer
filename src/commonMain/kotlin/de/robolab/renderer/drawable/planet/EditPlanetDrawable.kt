package de.robolab.renderer.drawable.planet

import de.robolab.planet.Path
import de.robolab.planet.Planet
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.edit.*
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class EditPlanetDrawable() : AbsPlanetDrawable() {

    var editCallback: IEditCallback = object : IEditCallback {}

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
        val position = pointer?.position ?: return
        menu = menu(position, name, init)
    }

    private val selectedPathControlPointsProperty = selectedElementsProperty.mapBinding {
        val path = selectedElement<Path>() ?: return@mapBinding null

        PathAnimatable.getControlPointsFromPath(path)
    }
    val selectedPathControlPoints by selectedPathControlPointsProperty
    var selectedPointEnd: EditDrawEndDrawable.PointEnd? = null


    var createPathWithCustomControlPoints = false
    var createPathControlPoints: List<Point> = emptyList()

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
                underlayers = listOf(
                        editPointDrawable
                ),
                planetLayers = listOf(
                        planetLayer
                ),
                overlayers = listOf(
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
