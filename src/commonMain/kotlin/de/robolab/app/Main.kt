package de.robolab.app

import de.robolab.file.PlanetFile
import de.robolab.file.demoFile
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.drawable.edit.EditPlanetDrawable
import de.robolab.renderer.platform.CommonTimer
import de.robolab.renderer.platform.ICanvas
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class Main(val canvas: ICanvas) {

    private val timer = CommonTimer(50.0)
    private val plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)
    private val planetDrawable = EditPlanetDrawable()

    private val animationTimer = CommonTimer(1000 / (ANIMATION_TIME * 1.25))

    val animateProperty = property(false)
    val editableProperty = planetDrawable.editableProperty
    val pointerProperty = plotter.pointerProperty.mapBinding {
        it.roundedPosition.toString() + " | " + it.objectsUnderPointer
    }

    init {
        plotter.drawable = planetDrawable

        animateProperty.onChange {
            if (animateProperty.value) {
                editableProperty.value = false
            }

            if (animateProperty.value) {
                animationTimer.start()
            } else {
                animationTimer.stop()
            }
        }
        editableProperty.onChange {
            if (editableProperty.value) {
                animateProperty.value = false
            }
        }

        val planetFile = PlanetFile(demoFile)
        planetDrawable.editCallback = planetFile

        var isUndoPhase = false

        animationTimer.onRender {
            if (isUndoPhase && !planetFile.history.canUndo) {
                isUndoPhase = false
            } else if (!isUndoPhase && !planetFile.history.canRedo) {
                isUndoPhase = true
            }
            if (isUndoPhase) {
                planetFile.history.undo()
            } else {
                planetFile.history.redo()
            }
        }

        planetFile.history.valueProperty.onChange {
            planetDrawable.importPlanet(planetFile.planet.value)
        }

        planetDrawable.importPlanet(planetFile.planet.value)
    }

    companion object {
        const val ANIMATION_TIME = 1000.0
    }
}