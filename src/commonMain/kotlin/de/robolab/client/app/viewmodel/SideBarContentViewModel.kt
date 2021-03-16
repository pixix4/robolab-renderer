package de.robolab.client.app.viewmodel

import de.westermann.kobserve.base.ObservableValue

interface SideBarContentViewModel: ViewModel {
    val nameProperty: ObservableValue<String>
    val parent: SideBarContentViewModel?
}
