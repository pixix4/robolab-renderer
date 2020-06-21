package de.robolab.client.app.controller

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarGroup
import de.robolab.client.app.model.base.INavigationBarPlottable
import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.app.model.group.GroupPlanetProvider
import de.robolab.client.app.model.room.RoomPlanetProvider
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.list.filterObservable
import de.westermann.kobserve.property.flattenBinding
import de.westermann.kobserve.property.flattenMutableBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class NavigationBarController(
    private val selectedEntryProperty: ObservableProperty<INavigationBarPlottable?>,
    messageManager: MessageManager,
    private val connection: RobolabMqttConnection,
    private val canvasController: CanvasController
) {

    private val filePlanetProvider = MultiFilePlanetProvider()
    private val groupPlanetProperty = GroupPlanetProvider(messageManager, filePlanetProvider)
    private val roomPlanetProvider = RoomPlanetProvider()

    val tabProperty = PreferenceStorage.selectedNavigationBarTabProperty

    val selectedGroupProperty = property<INavigationBarGroup?>(null)

    val selectedElementListProperty = selectedEntryProperty.mapBinding {
        var list = emptyList<INavigationBarEntry>()

        var elem: INavigationBarEntry? = it
        while (elem != null) {
            list = list + elem
            elem = elem.parent
        }

        list
    }

    val searchStringProperty = property(tabProperty, selectedGroupProperty) {
        val g = selectedGroupProperty.value

        if (g != null) {
            return@property property("")
        }

        when (tabProperty.value) {
            Tab.GROUP -> groupPlanetProperty.searchStringProperty
            Tab.ROOM -> roomPlanetProvider.searchStringProperty
            Tab.FILE -> filePlanetProvider.searchStringProperty
        }
    }.flattenMutableBinding()

    val entryListProperty = property(tabProperty, selectedGroupProperty) {
        val g = selectedGroupProperty.value

        if (g != null) {
            return@property g.entryList
        }

        when (tabProperty.value) {
            Tab.GROUP -> groupPlanetProperty.entryList
            Tab.ROOM -> roomPlanetProvider.entryList
            Tab.FILE -> filePlanetProvider.entryList
        }
    }.flattenBinding()

    val filteredEntryListProperty = entryListProperty.mapBinding {
        it.filterObservable(searchStringProperty) { element, filter ->
            element.titleProperty.value.contains(filter, true)
        }
    }

    fun open(entry: INavigationBarEntry) {
        when (entry) {
            is INavigationBarGroup -> {
                if (selectedGroupProperty.value == entry) {
                    selectedGroupProperty.value = entry.parent
                } else {
                    selectedGroupProperty.value = entry
                }
            }
            is INavigationBarPlottable -> {
                canvasController.open(entry)
            }
        }
    }

    fun closeGroup() {
        selectedGroupProperty.value = selectedGroupProperty.value?.parent
    }

    init {
        tabProperty.onChange {
            selectedGroupProperty.value = null
        }
    }

    val statusColor = connection.connectionStateProperty.mapBinding {
        when (it) {
            is RobolabMqttConnection.Connected -> StatusColor.SUCCESS
            is RobolabMqttConnection.Connecting -> StatusColor.WARN
            is RobolabMqttConnection.ConnectionLost -> StatusColor.ERROR
            is RobolabMqttConnection.Disconnected -> StatusColor.ERROR
            else -> StatusColor.ERROR
        }
    }

    val statusMessage = connection.connectionStateProperty.mapBinding {
        it.name
    }

    val statusActionLabel = connection.connectionStateProperty.mapBinding {
        it.actionLabel
    }

    fun onStatusAction() {
        connection.connectionState.onAction()
    }

    enum class StatusColor {
        SUCCESS,
        WARN,
        ERROR
    }

    enum class Tab(val label: String) {
        GROUP("Groups"),
        ROOM("Rooms"),
        FILE("Files")
    }
}
