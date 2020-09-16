package de.robolab.client.app.controller

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.SearchRequest
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.model.file.FileNavigationRoot
import de.robolab.client.app.model.group.GroupNavigationRoot
import de.robolab.client.app.model.room.RoomNavigationRoot
import de.robolab.client.app.repository.DatabaseMessageStorage
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.flatMapMutableBinding
import de.westermann.kobserve.property.mapBinding

class NavigationBarController(
    private val tabController: TabController,
    private val messageRepository: MessageRepository,
    messageManager: MessageManager,
) {

    private val filePlanetProvider = FileNavigationRoot(
        tabController
    )
    private val cachedFilePlanetProvider = CachedFilePlanetProvider(filePlanetProvider)
    private val groupPlanetProperty = GroupNavigationRoot(
        messageRepository,
        messageManager,
        tabController,
        cachedFilePlanetProvider
    )
    private val roomPlanetProvider = RoomNavigationRoot(
        messageRepository,
        tabController,
        cachedFilePlanetProvider
    )

    val tabProperty = PreferenceStorage.selectedNavigationBarTabProperty


    val searchStringProperty = tabProperty.flatMapMutableBinding {
        when (tabProperty.value) {
            Tab.GROUP -> groupPlanetProperty.searchProperty
            Tab.ROOM -> roomPlanetProvider.searchProperty
            Tab.FILE -> filePlanetProvider.searchProperty
        }
    }

    val searchRequestProperty =
        searchStringProperty.mapBinding { SearchRequest.parse(it) }


    val backButtonLabelProperty = tabProperty.flatMapBinding {
        when (tabProperty.value) {
            Tab.GROUP -> groupPlanetProperty.parentNameProperty
            Tab.ROOM -> roomPlanetProvider.parentNameProperty
            Tab.FILE -> filePlanetProvider.parentNameProperty
        }
    }

    fun onBackButtonClick() {
        when (tabProperty.value) {
            Tab.GROUP -> groupPlanetProperty.openParent()
            Tab.ROOM -> roomPlanetProvider.openParent()
            Tab.FILE -> filePlanetProvider.openParent()
        }
    }

    val entryListProperty = tabProperty.flatMapBinding {
        when (tabProperty.value) {
            Tab.GROUP -> groupPlanetProperty.childrenProperty
            Tab.ROOM -> roomPlanetProvider.childrenProperty
            Tab.FILE -> filePlanetProvider.childrenProperty
        }
    }

    enum class Tab(val label: String, val icon: MaterialIcon) {
        GROUP("MQTT Group list", MaterialIcon.GROUP),
        ROOM("All robot per planet", MaterialIcon.PUBLIC),
        FILE("List planet files", MaterialIcon.FOLDER_OPEN),
    }
}
