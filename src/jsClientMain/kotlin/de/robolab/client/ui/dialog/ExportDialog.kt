package de.robolab.client.ui.dialog

import de.robolab.client.app.model.file.FileEntryPlanetDocument
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.InputType
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.inputView

class ExportDialog private constructor(private val provider: FileEntryPlanetDocument) : Dialog("Export") {

    private val fileNameProperty = property(provider.planetFile.planet.name.trim())

    init {
        tab {
            dialogFormEntry("File name") {
                inputView(fileNameProperty)
            }

            dialogFormEntry("Export scale") {
                inputView(InputType.NUMBER, PreferenceStorage.exportScaleProperty.bindStringParsing()) {
                    min = 0.1
                    max = 100.0
                    step = 0.1
                }
            }

            dialogFormEntry("Export as") {
                buttonGroup {
                    button("SVG") {
                        onClick {
                            provider.exportAsSVG(fileNameProperty.value)
                        }
                    }
                    button("PNG") {
                        onClick {
                            provider.exportAsPNG(fileNameProperty.value)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open(provider: FileEntryPlanetDocument) {
            open(ExportDialog(provider))
        }
    }
}