package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.DialogController
import de.robolab.client.app.controller.MainController
import de.westermann.kobserve.list.observableListOf

class MainViewModel(
    private val controller: MainController
) : ViewModel {

    val toolBar = ToolBarViewModel(controller.toolBarController)

    val leftSideBar: SideBarViewModel = LeftSideBarViewModel(controller.navigationBarController)
    val rightSideBar: SideBarViewModel = RightSideBarViewModel(controller.infoBarController)

    val statusBar = StatusBarViewModel(controller.statusBarController)

    val content = ContentViewModel(controller.contentController, controller.uiController)

    val dialogList = observableListOf<DialogViewModel>()

    val terminal = TerminalViewModel()

    init {
        DialogController.onOpen {
            it.open(this)
        }
    }
}
