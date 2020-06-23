package de.robolab.client.ui.dialog

import de.robolab.client.utils.PreferenceStorage
import de.westermann.kwebview.components.InputType
import de.westermann.kwebview.components.inputView
import de.westermann.kwebview.components.selectView

class PaperConstraintsDialog private constructor() : Dialog("Paper constraints") {

    init {
        tab {
            dialogFormEntry("Orientation") {
                selectView(PreferenceStorage.paperOrientationProperty)
            }
            dialogFormEntry("Grid width") {
                inputView(InputType.NUMBER, PreferenceStorage.paperGridWidthProperty.bindStringParsing()) {
                    min = 0.1
                    max = 1000.0
                    step = 0.001
                }
            }
            dialogFormEntry("Paper strip width") {
                inputView(InputType.NUMBER, PreferenceStorage.paperStripWidthProperty.bindStringParsing()) {
                    min = 0.1
                    max = 1000.0
                    step = 0.001
                }
            }
            dialogFormEntry("Minimal padding") {
                inputView(InputType.NUMBER, PreferenceStorage.paperMinimalPaddingProperty.bindStringParsing()) {
                    min = 0.1
                    max = 1000.0
                    step = 0.001
                }
            }
            dialogFormEntry("Precision") {
                inputView(InputType.NUMBER, PreferenceStorage.paperPrecisionProperty.bindStringParsing()) {
                    min = 0.0
                    max = 10.0
                    step = 1.0
                }
            }
        }
    }

    companion object {
        fun open() {
            open(PaperConstraintsDialog())
        }
    }
}
