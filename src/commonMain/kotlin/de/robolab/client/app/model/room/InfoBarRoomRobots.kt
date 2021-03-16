package de.robolab.client.app.model.room

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class InfoBarRoomRobots(
    val groupStateList: ObservableValue<List<RoomPlanetDocument.GroupState>>,
    val uiController: UiController
) : SideBarTabViewModel("Groups", MaterialIcon.INFO_OUTLINE), SideBarContentViewModel {

    override val parent: SideBarContentViewModel? = null

    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)
    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent { }

}
