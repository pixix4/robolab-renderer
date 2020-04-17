package de.robolab.jfx.dialog

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.app.model.file.FilePlanetEntry
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.utils.buttonGroup
import de.robolab.jfx.utils.iconNoAdd
import de.robolab.utils.PreferenceStorage
import javafx.scene.text.FontWeight
import tornadofx.*

class ExportDialog() : View() {

    private val provider: FilePlanetEntry by param()
    private val fileNameProperty = de.westermann.kobserve.property.property(provider.planetFile.planet.name.trim())

    override val root = vbox {
        toolbar {
            style {
                padding = box(0.6.em, 1.5.em, 0.6.em, 1.5.em)
            }

            label("Export") {
                style {
                    fontWeight = FontWeight.BOLD
                    fontSize = 1.2.em
                }
            }
            spacer()
            button {
                graphic = iconNoAdd(MaterialIcon.CLOSE)

                setOnAction {
                    close()
                }
            }
        }

        vbox {
            style {
                padding = box(1.em)
            }

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
    }
}
