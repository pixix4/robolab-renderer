package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet
import de.westermann.kobserve.property.property

class TransformPlanetDialogViewModel(private val planetFile: PlanetFile) : DialogViewModel("Transform planet") {

    private val planetTranslateX = property(0)
    private val planetTranslateY = property(0)

    private val planetRotateX = property(planetFile.planet.startPoint?.point?.x ?: 0)
    private val planetRotateY = property(planetFile.planet.startPoint?.point?.y ?: 0)

    private val planetScaleFactor = property(1.0)
    private val planetScaleOffset = property(0)

    val content = buildForm {
        labeledGroup("Translate") {
            labeledEntry("Delta x") {
                input(planetTranslateX, -100000..100000)
            }
            labeledEntry("Delta y") {
                input(planetTranslateY, -100000..100000)
            }
            entry {
                button("Translate") {
                    planetFile.translate(Coordinate(planetTranslateX.value, planetTranslateY.value))
                }
            }
        }
        labeledGroup("Rotate") {
            labeledEntry("Origin x") {
                input(planetRotateX, -100000..100000)
            }
            labeledEntry("Origin y") {
                input(planetRotateY, -100000..100000)
            }
            entry {
                button("Clockwise") {
                    planetFile.rotate(
                        Planet.RotateDirection.CLOCKWISE,
                        Coordinate(planetRotateX.value, planetRotateY.value)
                    )
                }
                button("Counter clockwise") {
                    planetFile.rotate(
                        Planet.RotateDirection.COUNTER_CLOCKWISE,
                        Coordinate(planetRotateX.value, planetRotateY.value)
                    )
                }
            }
        }

        labeledGroup("Scale weights") {
            labeledEntry("Scale factor") {
                input(planetScaleFactor, -100000.0..100000.0, 1.0)
            }
            labeledEntry("Scale offset") {
                input(planetScaleOffset, -100000..100000)
            }
            entry {
                button("Scale") {
                    planetFile.scaleWeights(planetScaleFactor.value, planetScaleOffset.value)
                }
            }
        }
    }
}
