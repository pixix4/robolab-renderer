package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.controller.ui.UiController

class ContentViewModel(
    val contentController: ContentController,
    val uiController: UiController
) : ViewModel {

    fun setNavigationBarWidth(width: Double) {
        uiController.setNavigationBarWidth(width)
    }

    fun setInfoBarWidth(width: Double) {
        uiController.setInfoBarWidth(width)
    }
}
