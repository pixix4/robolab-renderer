package de.robolab.client.jfx.dialog

import de.robolab.client.app.model.file.FilePlanetEntry
import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import tornadofx.*

class ExportDialog : GenericDialog() {

    private val provider: FilePlanetEntry by param()
    private val fileNameProperty = de.westermann.kobserve.property.property(provider.planetFile.planet.name.trim())

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
