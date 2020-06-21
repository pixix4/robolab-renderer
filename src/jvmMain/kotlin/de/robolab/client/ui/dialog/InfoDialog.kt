package de.robolab.client.ui.dialog

import de.robolab.client.ui.adapter.toFx
import de.robolab.common.utils.BuildInformation
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.label


class InfoDialog : GenericDialog() {

    override val root = buildContent("Build information") {
        form {
            for ((topic, content) in BuildInformation.dataMap) {
                fieldset(topic) {
                    for ((key, value) in content) {
                        field(key) {
                            label(value.toFx())
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open() {
            open<InfoDialog>()
        }
    }
}
