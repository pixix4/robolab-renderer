package de.robolab.client.web.dialog

import de.robolab.client.theme.ThemePropertySelectorMapper
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.BuildInformation
import de.westermann.kobserve.not
import de.westermann.kwebview.components.*

class InfoDialog : Dialog("Build information") {

    override fun BoxView.buildContent() {
        for ((topic, content) in BuildInformation.dataMap) {
            dialogFormGroup(topic) {
                for ((key, value) in content) {
                    dialogFormEntry(key) {
                        textView(value)
                    }
                }
            }
        }
    }
}
