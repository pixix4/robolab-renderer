package de.robolab.jfx.dialog

import de.robolab.app.model.file.FilePlanetEntry
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.utils.buttonGroup
import de.robolab.utils.PreferenceStorage
import tornadofx.*

class ExportDialog() : GenericDialog() {

    private val provider: FilePlanetEntry by param()
    private val fileNameProperty = de.westermann.kobserve.property.property(provider.planetFile.planet.name.trim())

    override val root = buildContent("Export") {
        form {
            fieldset() {
                field("File name") {
                    textfield(fileNameProperty.toFx())
                }
                field("Export scale") {
                    textfield(PreferenceStorage.exportScaleProperty.toFx(), DoubleStringConverter(PreferenceStorage.exportScaleProperty.default))
                }
                field("Export as") {
                    buttonGroup {
                        button("SVG") {
                            setOnAction {
                                provider.exportAsSVG(fileNameProperty.value)
                                close()
                            }
                        }
                        button("PNG") {
                            setOnAction {
                                provider.exportAsPNG(fileNameProperty.value)
                                close()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open(provider: FilePlanetEntry) {
            open<ExportDialog>(
                    "provider" to provider
            )
        }
    }
}
