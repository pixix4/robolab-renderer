package de.robolab.web.views

import de.westermann.kobserve.Property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class InfoBar(infoBarActiveProperty: Property<Boolean>) : ViewCollection<View>() {

    init {
        classList.bind("active", infoBarActiveProperty)

        // Close info bar on mobile
        onClick {
            if (it.target == html && infoBarActiveProperty.value) {
                infoBarActiveProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}
