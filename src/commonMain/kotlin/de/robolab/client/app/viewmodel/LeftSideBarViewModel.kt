package de.robolab.client.app.viewmodel

import de.robolab.client.app.controller.ui.NavigationBarController
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.property

class LeftSideBarViewModel(private val navigationBarController: NavigationBarController) : SideBarViewModel {

    override val tabList = observableListOf<SideBarTabViewModel>()

    override val activeTab: ObservableProperty<SideBarTabViewModel?> = property()

    override fun open(tab: SideBarTabViewModel) {
        if (tab in tabList) {
            activeTab.value = tab
        } else {
            activeTab.value = tabList.firstOrNull() ?: return
        }
    }

    override fun closeSideBar() {
        navigationBarController.closeSideBar()
    }

    init {
        activeTab.onChange {
            val index = activeTab.value?.let { tabList.indexOf(it) } ?: 0
            navigationBarController.tabIndexProperty.value = index
        }

        tabList.addAll(navigationBarController.tabListProperty.value)
        activeTab.value = tabList.getOrNull(navigationBarController.tabIndexProperty.value) ?: tabList.firstOrNull()

        navigationBarController.tabListProperty.onChange {
            tabList.sync(navigationBarController.tabListProperty.value)
        }
    }
}
