package de.robolab.client.app.controller.ui

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.model.file.FileNavigationTab
import de.robolab.client.app.model.group.GroupNavigationTab
import de.robolab.client.app.model.room.RoomNavigationTab
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.property.property

class NavigationBarController(
    private val contentController: ContentController,
    messageRepository: MessageRepository,
    messageManager: MessageManager,
    private val filePlanetController: FilePlanetController,
    private val uiController: UiController
) {

    private val groupPlanetProperty = GroupNavigationTab(
        messageRepository,
        messageManager,
        contentController,
        filePlanetController,
        uiController
    )
    private val roomPlanetProvider = RoomNavigationTab(
        messageRepository,
        contentController,
        filePlanetController,
        uiController
    )

    val tabListProperty = property(
        listOf(
            groupPlanetProperty,
            roomPlanetProvider
        )
    )
    val tabIndexProperty = PreferenceStorage.selectedNavigationBarTabProperty

    private fun updateList() {
        tabListProperty.value = mutableListOf(
            groupPlanetProperty,
            roomPlanetProvider
        ) + listOfNotNull(
            filePlanetController.localServerController.localPlanetLoaderProperty.value,
            filePlanetController.remoteServerController.remotePlanetLoaderProperty.value
        ).map {
            FileNavigationTab(contentController, filePlanetController, it, uiController)
        }
    }

    fun closeSideBar() {
        uiController.navigationBarEnabledProperty.value = true
    }

    init {
        updateList()
        filePlanetController.localServerController.localPlanetLoaderProperty.onChange {
            updateList()
        }
        filePlanetController.remoteServerController.remotePlanetLoaderProperty.onChange {
            updateList()
        }
    }
}
