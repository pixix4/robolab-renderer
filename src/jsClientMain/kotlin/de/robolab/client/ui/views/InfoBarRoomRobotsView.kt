package de.robolab.client.ui.views

import de.robolab.client.app.model.room.InfoBarRoomRobots
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarRoomRobotsView(private val content: InfoBarRoomRobots) : ViewCollection<View>() {

    private lateinit var box: BoxView

    private fun update() {
        box.clear()

        val list = content.groupStateList.value
        for (state in list) {
            boxView {
                textView(state.attempt.groupName)
                textView(state.description())
            }
        }
    }

    init {
        scrollBoxView {
            box = resizeBox(1.0) {
                update()
                content.groupStateList.onChange {
                    update()
                }
            }
        }
    }
}
