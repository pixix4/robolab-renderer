package de.robolab.client.app.controller.ui

import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.list.flatten
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.flatMapMutableBinding
import de.westermann.kobserve.property.mapBinding

class InfoBarController(
    contentController: ContentController,
    private val uiController: UiController
) {

    private val activeDocumentProperty = contentController.activeTabProperty.flatMapBinding { it.documentProperty }

    val tabListProperty: ObservableList<SideBarTabViewModel> = activeDocumentProperty
        .mapBinding { it.infoBarTabs }.flatten()

    val activeTabProperty: ObservableProperty<SideBarTabViewModel?> = activeDocumentProperty
        .flatMapMutableBinding { it.activeTabProperty }

    fun open(tab: SideBarTabViewModel) {
        activeDocumentProperty.value.openTab(tab)
    }

    fun closeSideBar() {
        uiController.infoBarEnabledProperty.value = false
    }
}
