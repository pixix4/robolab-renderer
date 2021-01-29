package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.file.details.*
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFileTestView(
    private val content: InfoBarFileTest,
    private val uiController: UiController
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {
                textView("Hey")
            }
        }
    }
}
