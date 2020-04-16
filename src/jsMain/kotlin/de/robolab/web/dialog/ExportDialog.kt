package de.robolab.web.dialog

import de.robolab.app.model.file.FilePlanetEntry
import de.robolab.utils.PreferenceStorage
import de.robolab.web.views.utils.buttonGroup
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.InputType
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.inputView

class ExportDialog(private val provider: FilePlanetEntry) : Dialog("Export") {

    private val fileNameProperty = property(provider.planetFile.planet.name.trim())

    override fun BoxView.buildContent() {
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
