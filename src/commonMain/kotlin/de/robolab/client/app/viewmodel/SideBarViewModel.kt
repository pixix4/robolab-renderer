package de.robolab.client.app.viewmodel

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property

interface SideBarViewModel : ViewModel {

    val tabList: ObservableList<SideBarTabViewModel>
    val activeTab: ObservableValue<SideBarTabViewModel?>

    fun open(tab: SideBarTabViewModel)

    fun closeSideBar()
}
