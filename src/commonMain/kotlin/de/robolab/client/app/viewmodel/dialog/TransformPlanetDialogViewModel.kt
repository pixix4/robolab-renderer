package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.common.planet.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.westermann.kobserve.property.property

class TransformPlanetDialogViewModel(private val planetFile: PlanetFile) : DialogViewModel("Transform planet") {

    private val planetTranslateX = property(0L)
    private val planetTranslateY = property(0L)

    private val planetRotateX = property(planetFile.planet.startPoint.x)
    private val planetRotateY = property(planetFile.planet.startPoint.y)

    private val planetScaleFactor = property(1.0)
    private val planetScaleOffset = property(0L)

    val content = buildForm {
        labeledGroup("Translate") {
            labeledEntry("Delta x") {
                input(planetTranslateX, -100000L..100000L)
            }
            labeledEntry("Delta y") {
                input(planetTranslateY, -100000L..100000L)
            }
            entry {
                button("Translate") {
                    planetFile.translate(PlanetPoint(planetTranslateX.value, planetTranslateY.value))
                }
            }
        }
        labeledGroup("Rotate") {
            labeledEntry("Origin x") {
                input(planetRotateX, -100000L..100000L)
            }
            labeledEntry("Origin y") {
                input(planetRotateY, -100000L..100000L)
            }
            entry {
                button("Clockwise") {
                    planetFile.rotate(
                        Planet.RotateDirection.CLOCKWISE,
                        PlanetPoint(planetRotateX.value, planetRotateY.value)
                    )
                }
                button("Counter clockwise") {
                    planetFile.rotate(
                        Planet.RotateDirection.COUNTER_CLOCKWISE,
                        PlanetPoint(planetRotateX.value, planetRotateY.value)
                    )
                }
            }
        }

        labeledGroup("Scale weights") {
            labeledEntry("Scale factor") {
                input(planetScaleFactor, -100000.0..100000.0, 1.0)
            }
            labeledEntry("Scale offset") {
                input(planetScaleOffset, -100000L..100000L)
            }
            entry {
                button("Scale") {
                    planetFile.scaleWeights(planetScaleFactor.value, planetScaleOffset.value)
                }
            }
        }
    }
}
