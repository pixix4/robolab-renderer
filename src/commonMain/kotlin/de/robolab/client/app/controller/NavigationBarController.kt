package de.robolab.client.app.controller

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.model.file.FileNavigationRoot
import de.robolab.client.app.model.group.GroupNavigationRoot
import de.robolab.client.app.model.room.RoomNavigationRoot
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.flatMapMutableBinding
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.property

class NavigationBarController(
    tabController: TabController,
    messageRepository: MessageRepository,
    messageManager: MessageManager,
) {

    val filePlanetProvider = FileNavigationRoot(tabController)
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

    val tabListProperty = property(
        listOf<Tab>(
            groupPlanetProperty,
            roomPlanetProvider
        )
    )
    val tabIndexProperty = PreferenceStorage.selectedNavigationBarTabProperty
    val tabProperty = tabListProperty.join(tabIndexProperty) { list, index ->
        list.getOrNull(index) ?: list.lastOrNull() ?: EmptyTab
    }

    fun updateList() {
        val newList = mutableListOf<Tab>(
            groupPlanetProperty,
            roomPlanetProvider
        )

        newList += filePlanetProvider.fileNavigationList.value

        tabListProperty.value = newList
    }

    val searchStringProperty = tabProperty.flatMapMutableBinding {
        it.searchProperty
    }

    fun submitSearch() {
        tabProperty.value.submitSearch()
    }

    val backButtonLabelProperty = tabProperty.flatMapBinding {
        it.parentNameProperty
    }

    fun onBackButtonClick() {
        tabProperty.value.openParent()
    }

    val entryListProperty = tabProperty.flatMapBinding {
        it.childrenProperty
    }

    interface Tab {
        val label: ObservableValue<String>
        val icon: ObservableValue<MaterialIcon>

        val searchProperty: ObservableProperty<String>
        val parentNameProperty: ObservableValue<String?>
        val childrenProperty: ObservableValue<ObservableList<INavigationBarEntry>>

        fun openParent()

        fun submitSearch()
    }

    object EmptyTab : Tab {
        override val label = property("")
        override val icon = property(MaterialIcon.CANCEL)
        override val searchProperty = property("")
        override val parentNameProperty: ObservableValue<String?> = property()
        override val childrenProperty: ObservableValue<ObservableList<INavigationBarEntry>> =
            property(observableListOf())

        override fun openParent() {}
        override fun submitSearch() {}
    }

    init {
        updateList()
        filePlanetProvider.fileNavigationList.onChange {
            updateList()
        }
    }
}
