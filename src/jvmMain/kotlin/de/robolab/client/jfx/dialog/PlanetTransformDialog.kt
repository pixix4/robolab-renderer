package de.robolab.client.jfx.dialog

import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

class PlanetTransformDialog : GenericDialog() {

    private val planetFile: PlanetFile by param()

    private val planetTranslateX = SimpleObjectProperty(0)
    private val planetTranslateY = SimpleObjectProperty(0)

    private val planetRotateX = SimpleObjectProperty(planetFile.planet.startPoint?.point?.x ?: 0)
    private val planetRotateY = SimpleObjectProperty(planetFile.planet.startPoint?.point?.y ?: 0)

    private val planetScaleFactor = SimpleObjectProperty(1.0)
    private val planetScaleOffset = SimpleObjectProperty(0)

    override val root = buildContent("Export") {
        form {
            fieldset("Translate") {
                field("Delta x") {
                    textfield(planetTranslateX, IntStringConverter(planetTranslateX.value))
                }
                field("Delta y") {
                    textfield(planetTranslateY, IntStringConverter(planetTranslateY.value))
                }
                field(forceLabelIndent = true) {
                    button("Translate") {
                        setOnAction {
                            planetFile.translate(Coordinate(planetTranslateX.value, planetTranslateY.value))
                            close()
                        }
                    }
                }
            }
            fieldset("Rotate") {
                field("Origin x") {
                    textfield(planetRotateX, IntStringConverter(planetRotateX.value))
                }
                field("Origin y") {
                    textfield(planetRotateY, IntStringConverter(planetRotateY.value))
                }
                field(forceLabelIndent = true) {
                    button("Clockwise") {
                        setOnAction {
                            planetFile.rotate(
                                Planet.RotateDirection.CLOCKWISE,
                                Coordinate(planetRotateX.value, planetRotateY.value)
                            )
                            close()
                        }
                    }
                    button("Counter clockwise") {
                        setOnAction {
                            planetFile.rotate(
                                Planet.RotateDirection.COUNTER_CLOCKWISE,
                                Coordinate(planetRotateX.value, planetRotateY.value)
                            )
                            close()
                        }
                    }
                }
            }
            fieldset("Scale weights") {
                field("Scale factor") {
                    textfield(planetScaleFactor, DoubleStringConverter(planetScaleFactor.value))
                }
                field("Scale offset") {
                    textfield(planetScaleOffset, IntStringConverter(planetScaleOffset.value))
                }
                field(forceLabelIndent = true) {
                    button("Scale") {
                        setOnAction {
                            planetFile.scaleWeights(planetScaleFactor.value, planetScaleOffset.value)
                            close()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open(planetFile: PlanetFile) {
            open<PlanetTransformDialog>(
                "planetFile" to planetFile
            )
        }
    }
}
