package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.drawable.edit.*
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.view.base.menu
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.property

class EditPlanetDrawable(
    private val editCallback: IEditCallback,
    transformationStateProperty: ObservableProperty<Transformation.State> = property(Transformation.State.DEFAULT)
) : AbsPlanetDrawable(transformationStateProperty) {

    private val createPath = CreatePathManager(editCallback)
    private val editPoints = EditPointsManager(editCallback)
    private val editPointEnds = EditPointEndsManager(editCallback, createPath)

    private val planetLayer = EditPlanetLayer(editCallback, editPointEnds)

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)

        editPoints.importPlanet(planet)
        editPointEnds.importPlanet(planet)

        importPlanets()
    }

    init {
        setPlanetLayers(planetLayer)

        underlayerViews.add(editPoints.view)

        overlayerViews.add(createPath.view)

        view.onKeyPress { event ->
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
                }
            }
        }

        view.onPointerSecondaryAction { event ->
            view.menu(event, "Planet") {
                action("Create comment") {
                    editCallback.createComment(listOf("Comment"), event.planetPoint)
                }
            }

            event.stopPropagation()
        }
    }
}
