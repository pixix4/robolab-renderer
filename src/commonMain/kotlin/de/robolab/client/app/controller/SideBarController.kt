package de.robolab.client.app.controller

import de.robolab.client.app.model.ISideBarEntry
import de.robolab.client.app.model.ISideBarGroup
import de.robolab.client.app.model.ISideBarPlottable
import de.robolab.client.app.model.file.FilePlanetProvider
import de.robolab.client.app.model.group.GroupPlanetProvider
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.list.filterObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.flattenMutableBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class SideBarController(
    val selectedEntryProperty: ObservableProperty<ISideBarPlottable?>,
    messageManager: MessageManager,
    private val connection: RobolabMqttConnection
) {

    private val filePlanetProvider = FilePlanetProvider()
    private val groupPlanetProperty = GroupPlanetProvider(messageManager, filePlanetProvider)

    val tabProperty = PreferenceStorage.selectedSideBarTabProperty

    val selectedGroupProperty = property<ISideBarGroup?>(null)

    val selectedElementListProperty = selectedEntryProperty.mapBinding {
        var list = emptyList<ISideBarEntry>()

        var elem: ISideBarEntry? = it
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
            Tab.PLANET -> property("")
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
            Tab.PLANET -> observableListOf()
            Tab.FILE -> filePlanetProvider.entryList
        }
    }

    val filteredEntryListProperty = entryListProperty.mapBinding {
        it.filterObservable(searchStringProperty) { element, filter ->
            element.titleProperty.value.contains(filter, true)
        }
    }

    fun open(entry: ISideBarEntry) {
        when (entry) {
            is ISideBarGroup -> {
                if (selectedGroupProperty.value == entry) {
                    selectedGroupProperty.value = entry.parent
                } else {
                    selectedGroupProperty.value = entry
                }
            }
            is ISideBarPlottable -> {
                val old = selectedEntryProperty.value
                entry.onOpen()
                selectedEntryProperty.value = entry
                old?.onClose()
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
        PLANET("Planets"),
        FILE("Files")
    }
}
