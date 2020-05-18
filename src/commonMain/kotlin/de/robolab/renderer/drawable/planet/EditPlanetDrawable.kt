package de.robolab.renderer.drawable.planet

import de.robolab.planet.Path
import de.robolab.planet.Planet
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.ConditionalView
import de.robolab.renderer.drawable.edit.EditPaperBackground
import de.robolab.renderer.drawable.edit.IEditCallback
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.utils.MenuBuilder
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class EditPlanetDrawable(
        private val editCallback: IEditCallback
) : AbsPlanetDrawable() {

    val editableProperty = property(false)
    val editable by editableProperty

    private val editCallbackProperty = editableProperty.mapBinding {
        if (it) editCallback else null
    }

    fun menu(name: String, init: MenuBuilder.() -> Unit) {
        val position = pointer?.position ?: return
        val contextMenu = de.robolab.utils.menu(position, name, init)
        plotter?.context?.openContextMenu(contextMenu)
    }


    private val planetLayer = EditPlanetLayer(editCallbackProperty)
    private val paperBackground = EditPaperBackground()

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)

        paperBackground.importPlanet(planet)

        importPlanets()
    }

    init {
        setPlanetLayers(planetLayer)

        backgroundViews.add(ConditionalView("Paper background", PreferenceStorage.paperBackgroundEnabledProperty, paperBackground.backgroundView))
        underlayerViews.add(ConditionalView("Paper measuring views", PreferenceStorage.paperBackgroundEnabledProperty, paperBackground.measuringView))
        drawBackgroundProperty.bind(!PreferenceStorage.paperBackgroundEnabledProperty)

        view.onKeyPress { event ->
            if (!editable) return@onKeyPress

            when (event.keyCode) {
                KeyCode.UNDO -> {
                    if (editable) {
                        editCallback.undo()
                    }
                }
                KeyCode.REDO -> {
                    if (editable) {
                        editCallback.redo()
                    }
                }
                KeyCode.Z -> if (event.ctrlKey) {
                    if (editable) {
                        if (event.shiftKey) {
                            editCallback.redo()
                        } else {
                            editCallback.undo()
                        }
                    }
                }
                else -> {
                }
            }
        }
    }
}
