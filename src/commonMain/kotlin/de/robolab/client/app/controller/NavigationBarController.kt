package de.robolab.client.app.controller

import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.model.file.FileNavigationManager
import de.robolab.client.app.model.group.GroupNavigationTab
import de.robolab.client.app.model.room.RoomNavigationTab
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.property.*

class NavigationBarController(
    tabController: TabController,
    messageRepository: MessageRepository,
    messageManager: MessageManager,
) {

    val filePlanetProvider = FileNavigationManager(tabController)
    private val cachedFilePlanetProvider = CachedFilePlanetProvider(filePlanetProvider)
    val groupPlanetProperty = GroupNavigationTab(
        messageRepository,
        messageManager,
        tabController,
        cachedFilePlanetProvider
    )
    private val roomPlanetProvider = RoomNavigationTab(
        messageRepository,
        tabController,
        cachedFilePlanetProvider
    )

    val tabListProperty = property(
        listOf(
            groupPlanetProperty,
            roomPlanetProvider
        )
    )
    val tabIndexProperty = PreferenceStorage.selectedNavigationBarTabProperty
    val tabProperty = tabListProperty.join(tabIndexProperty) { list, index ->
        list.getOrNull(index) ?: list.lastOrNull() ?: EmptyNavigationBarTab
    }

    private fun updateList() {
        val newList = mutableListOf(
            groupPlanetProperty,
            roomPlanetProvider
        )

        newList += filePlanetProvider.fileNavigationList.value

        tabListProperty.value = newList
    }

    val supportedModes = tabProperty.mapBinding { it.supportedModes }
    val modeProperty = tabProperty.flatMapBinding { it.modeProperty }
    fun selectMode(mode: String) = tabProperty.value.selectMode(mode)

    val searchStringProperty = tabProperty.flatMapMutableBinding {
        it.searchProperty
    }

    fun submitSearch() {
        tabProperty.value.submitSearch()
    }

    val backButtonLabelProperty = tabProperty.flatMapBinding {
        it.labelProperty
    }

    val backButtonEnabledProperty = tabProperty.flatMapBinding {
        it.canGoBackProperty
    }

    fun onBackButtonClick() {
        tabProperty.value.goBack()
    }

    val entryListProperty = tabProperty.flatMapBinding {
        it.childrenProperty
    }

    init {
        updateList()
        filePlanetProvider.fileNavigationList.onChange {
            updateList()
        }
    }
}
