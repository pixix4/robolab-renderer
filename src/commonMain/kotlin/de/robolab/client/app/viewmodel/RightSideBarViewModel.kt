package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.ui.InfoBarController

class RightSideBarViewModel(
    private val infoBarController: InfoBarController
) : SideBarViewModel {
    override val tabList = infoBarController.tabListProperty
    override val activeTab = infoBarController.activeTabProperty

    override fun open(tab: SideBarTabViewModel) {
        infoBarController.open(tab)
    }

    override fun closeSideBar() {
        infoBarController.closeSideBar()
    }
}
