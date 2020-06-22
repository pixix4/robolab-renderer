package de.robolab.client.ui.dialog

import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.InputType
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.inputView

class PlanetTransformDialog(private val planetFile: PlanetFile) : Dialog("Export") {

    private val planetTranslateX = property(0)
    private val planetTranslateY = property(0)

    private val planetRotateX = property(planetFile.planet.startPoint?.point?.x ?: 0)
    private val planetRotateY = property(planetFile.planet.startPoint?.point?.y ?: 0)

    private val planetScaleFactor = property(1.0)
    private val planetScaleOffset = property(0)

    init {
        tab {
            dialogFormGroup("Translate") {
                dialogFormEntry("Delta x") {
                    inputView(InputType.NUMBER, planetTranslateX.bindStringParsing()) {
                        min = -100000.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("Delta y") {
                    inputView(InputType.NUMBER, planetTranslateY.bindStringParsing()) {
                        min = -100000.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("") {
                    button("Translate") {
                        onClick {
                            planetFile.translate(Coordinate(planetTranslateX.value, planetTranslateY.value))
                        }
                    }
                }
            }

            dialogFormGroup("Rotate") {
                dialogFormEntry("Origin x") {
                    inputView(InputType.NUMBER, planetRotateX.bindStringParsing()) {
                        min = -100000.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("Origin y") {
                    inputView(InputType.NUMBER, planetRotateY.bindStringParsing()) {
                        min = -100000.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("") {
                    button("Clockwise") {
                        onClick {
                            planetFile.rotate(
                                Planet.RotateDirection.CLOCKWISE,
                                Coordinate(planetRotateX.value, planetRotateY.value)
                            )
                        }
                    }
                    button("Counter clockwise") {
                        onClick {
                            planetFile.rotate(
                                Planet.RotateDirection.COUNTER_CLOCKWISE,
                                Coordinate(planetRotateX.value, planetRotateY.value)
                            )
                        }
                    }
                }
            }

            dialogFormGroup("Scale weights") {
                dialogFormEntry("Scale factor") {
                    inputView(InputType.NUMBER, planetScaleFactor.bindStringParsing()) {
                        min = -100000.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("Scale offset") {
                    inputView(InputType.NUMBER, planetScaleOffset.bindStringParsing()) {
                        min = -100000.0
                        max = 100000.0
                        step = 1.0
                    }
                }
                dialogFormEntry("") {
                    button("Scale") {
                        onClick {
                            planetFile.scaleWeights(planetScaleFactor.value, planetScaleOffset.value)
                        }
                    }
                }
            }
        }
    }
}
