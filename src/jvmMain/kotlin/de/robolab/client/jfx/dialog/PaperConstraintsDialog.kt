package de.robolab.client.jfx.dialog

import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.renderer.drawable.edit.EditPaperBackground
import de.robolab.client.utils.PreferenceStorage
import tornadofx.*

class PaperConstraintsDialog : GenericDialog() {

    override val root = buildContent("Paper constraints") {
        form {
            fieldset {
                field("Paper orientation") {
                    combobox(
                        PreferenceStorage.paperOrientationProperty.toFx(),
                        EditPaperBackground.Orientation.values().toList()
                    )
                }
                field("Grid width") {
                    textfield(
                        PreferenceStorage.paperGridWidthProperty.toFx(),
                        DoubleStringConverter(PreferenceStorage.paperGridWidthProperty.default)
                    )
                }
                field("Paper strip width") {
                    textfield(
                        PreferenceStorage.paperStripWidthProperty.toFx(),
                        DoubleStringConverter(PreferenceStorage.paperStripWidthProperty.default)
                    )
                }
                field("Minimal padding") {
                    textfield(
                        PreferenceStorage.paperMinimalPaddingProperty.toFx(), DoubleStringConverter(
                            PreferenceStorage.paperMinimalPaddingProperty.default
                        )
                    )
                }
                field("Precision") {
                    textfield(
                        PreferenceStorage.paperPrecisionProperty.toFx(),
                        IntStringConverter(PreferenceStorage.paperPrecisionProperty.default)
                    )
                }
            }
        }
    }

    companion object {
        fun open() {
            open<PaperConstraintsDialog>()
        }
    }
}