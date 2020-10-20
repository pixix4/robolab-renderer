package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.room.InfoBarRoomRobots
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarRoomRobotsView(
    private val content: InfoBarRoomRobots,
    private val uiController: UiController
) : ViewCollection<View>() {


    private fun update(box: BoxView) {
        box.clear()

        val list = content.groupStateList.value
        for (state in list) {
            box.boxView {
                textView(state.attempt.groupName)
                textView(state.description())
            }
        }
    }

    init {
        scrollBoxView {
            uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {
                update(this)
                content.groupStateList.onChange {
                    update(this)
                }
            }
        }
    }
}
