package de.robolab.renderer.drawable.edit

import de.robolab.model.Planet
import de.robolab.renderer.Pointer
import de.robolab.renderer.drawable.PathDrawable
import de.robolab.renderer.drawable.PlanetDrawable
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
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
    private val editMenuDrawable = EditMenuDrawable(this)

    val editableProperty = property(false)
    val editable by editableProperty

    val menuProperty = property<Menu?>(null)
    var menu by menuProperty

    fun menu(name: String, init: MenuBuilder.() -> Unit) {
        menu = menu(pointer.position, name, init)
    }

    private val selectedPathControlPointsProperty = selectedPathProperty.mapBinding { nullablePath ->
        val path = nullablePath ?: return@mapBinding null

        PathDrawable.getControlPointsFromPath(path)
    }
    val selectedPathControlPoints by selectedPathControlPointsProperty
    var selectedPointEnd: EditDrawEndDrawable.PointEnd? = null

    override val drawableList = listOf(
            planetBackground,
            *viewBackground.toTypedArray(),
            editPointDrawable,
            *planetForeground.toTypedArray(),
            editDrawEndDrawable,
            editPathSelectDrawable,
            editPathDrawable,
            editControlPointsDrawable,
            *viewForeground.toTypedArray(),
            editMenuDrawable
    )

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
