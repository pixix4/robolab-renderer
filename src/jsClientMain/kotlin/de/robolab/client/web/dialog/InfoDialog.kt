package de.robolab.client.web.dialog

import de.robolab.common.utils.BuildInformation
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.inputView

class InfoDialog : Dialog("Build information") {

    override fun BoxView.buildContent() {
        for ((topic, content) in BuildInformation.dataMap) {
            dialogFormGroup(topic) {
                for ((key, value) in content) {
                    dialogFormEntry(key) {
                        inputView(value.mapBinding { it.toString() }) {
                            readonly = true
                        }
                    }
                }
            }
        }
    }
}
