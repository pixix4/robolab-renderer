package de.robolab.jfx.dialog

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.utils.iconNoAdd
import de.robolab.renderer.drawable.edit.EditPaperBackground
import de.robolab.utils.PreferenceStorage
import javafx.scene.text.FontWeight
import tornadofx.*

class PaperConstraintsDialog : View() {

    override val root = vbox {
        toolbar {
            style {
                padding = box(0.6.em, 1.5.em, 0.6.em, 1.5.em)
            }

            label("Paper constraints") {
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
                    field("Paper orientation") {
                        combobox(PreferenceStorage.paperOrientationProperty.toFx(), EditPaperBackground.Orientation.values().toList())
                    }
                    field("Grid width") {
                        textfield(PreferenceStorage.paperGridWidthProperty.toFx(), DoubleStringConverter(PreferenceStorage.paperGridWidthProperty.default))
                    }
                    field("Paper strip width") {
                        textfield(PreferenceStorage.paperStripWidthProperty.toFx(), DoubleStringConverter(PreferenceStorage.paperStripWidthProperty.default))
                    }
                    field("Minimal padding") {
                        textfield(PreferenceStorage.paperMinimalPaddingProperty.toFx(), DoubleStringConverter(PreferenceStorage.paperMinimalPaddingProperty.default))
                    }
                }
            }
        }
    }
}
