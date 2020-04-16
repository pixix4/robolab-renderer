package de.robolab.web.dialog

import de.robolab.utils.PreferenceStorage
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.InputType
import de.westermann.kwebview.components.inputView
import de.westermann.kwebview.components.selectView

class PaperConstraintsDialog() : Dialog("Paper constraints") {

    override fun BoxView.buildContent() {
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
    }
}
