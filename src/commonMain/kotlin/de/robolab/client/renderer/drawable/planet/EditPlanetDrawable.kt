package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.drawable.edit.CreatePathManager
import de.robolab.client.renderer.drawable.edit.EditPaperBackground
import de.robolab.client.renderer.drawable.edit.EditPointsManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.ConditionalView
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.planet.Planet
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

    private val paperBackground = EditPaperBackground()

    private val createPath = CreatePathManager(editCallbackProperty)
    private val editPoints = EditPointsManager(editCallbackProperty, createPath)

    private val planetLayer = EditPlanetLayer(editCallbackProperty, createPath)

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)

        paperBackground.importPlanet(planet)
        editPoints.importPlanet(planet)

        importPlanets()
    }

    init {
        setPlanetLayers(planetLayer)

        backgroundViews.add(
            ConditionalView(
                "Paper background",
                PreferenceStorage.paperBackgroundEnabledProperty,
                paperBackground.backgroundView
            )
        )
        underlayerViews.add(
            ConditionalView(
                "Paper measuring views",
                PreferenceStorage.paperBackgroundEnabledProperty,
                paperBackground.measuringView
            )
        )
        drawBackgroundProperty.bind(!PreferenceStorage.paperBackgroundEnabledProperty)

        underlayerViews.add(editPoints.view)

        overlayerViews.add(createPath.view)

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

        view.onPointerSecondaryAction { event ->
            val callback = editCallbackProperty.value ?: return@onPointerSecondaryAction

            view.menu(event, "Planet") {
                action("Create comment") {
                    callback.createComment(listOf("Comment"), event.planetPoint)
                }
            }

            event.stopPropagation()
        }
    }
}
