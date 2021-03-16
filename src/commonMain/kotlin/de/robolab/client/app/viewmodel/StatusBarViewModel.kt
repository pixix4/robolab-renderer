package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.ui.StatusBarController

class StatusBarViewModel(
    controller: StatusBarController
): ViewModel {

    val connectionList = controller.connectionList
    val contentList = controller.contentList
    val progressList = controller.progressList
}
