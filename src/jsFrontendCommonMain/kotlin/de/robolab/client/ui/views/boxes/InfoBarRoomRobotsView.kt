package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.room.InfoBarRoomRobots
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarRoomRobotsView(
    private val viewModel: InfoBarRoomRobots
) : ViewCollection<View>() {


    private fun update(box: BoxView) {
        box.clear()

        val list = viewModel.groupStateList.value
        for (state in list) {
            box.boxView {
                textView(state.attempt.groupName)
                textView(state.description())
            }
        }
    }

    init {
        scrollBoxView {
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {
                update(this)
                viewModel.groupStateList.onChange {
                    update(this)
                }
            }
        }
    }

    companion object: ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarRoomRobots
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarRoomRobotsView(viewModel as InfoBarRoomRobots)
        }
    }
}
