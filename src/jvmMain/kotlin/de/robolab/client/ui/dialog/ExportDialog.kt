package de.robolab.client.ui.dialog

import de.robolab.client.app.model.file.FileEntryPlanetDocument
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import tornadofx.*

class ExportDialog : GenericDialog() {

    private val planetDocument: FileEntryPlanetDocument by param()
    private val fileNameProperty = de.westermann.kobserve.property.property("")

    override val root = buildContent("Export") {
        form {
            fieldset {
                field("File name") {
                    textfield(fileNameProperty.toFx())
                }
                field("Export scale") {
                    textfield(
                        PreferenceStorage.exportScaleProperty.toFx(),
                        DoubleStringConverter(PreferenceStorage.exportScaleProperty.default)
                    )
                }
                field("Export as") {
                    buttonGroup {
                        button("SVG") {
                            setOnAction {
                                planetDocument.exportAsSVG(fileNameProperty.value)
                                close()
                            }
                        }
                        button("PNG") {
                            setOnAction {
                                planetDocument.exportAsPNG(fileNameProperty.value)
                                close()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()

        fileNameProperty.value = planetDocument.planetFile.planet.name.trim()
    }

    companion object {
        fun open(planetDocument: FileEntryPlanetDocument) {
            open<ExportDialog>(
                "planetDocument" to planetDocument
            )
        }
    }
}
