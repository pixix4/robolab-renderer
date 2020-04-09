package de.robolab.app.controller

import de.robolab.app.model.ISideBarEntry
import de.robolab.app.model.ISideBarGroup
import de.robolab.app.model.ISideBarPlottable
import de.robolab.app.model.file.FilePlanetProvider
import de.robolab.app.model.group.GroupPlanetProvider
import de.robolab.communication.MessageManager
import de.robolab.communication.mqtt.RobolabMqttConnection
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.Property
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class SideBarController(
        val selectedEntryProperty: Property<ISideBarPlottable?>,
        messageManager: MessageManager,
        private val connection: RobolabMqttConnection
) {

    private val groupPlanetProperty = GroupPlanetProvider(messageManager)
    private val filePlanetProvider = FilePlanetProvider()

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
                selectedEntryProperty.value = entry
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
        when (it) {
            is RobolabMqttConnection.Connected -> "Connected"
            is RobolabMqttConnection.Connecting -> "Connecting"
            is RobolabMqttConnection.ConnectionLost -> "Connection lost"
            is RobolabMqttConnection.Disconnected -> "Disconnected"
            else -> ""
        }
    }

    val statusActionLabel = connection.connectionStateProperty.mapBinding {
        when (it) {
            is RobolabMqttConnection.Connected -> "Disconnect"
            is RobolabMqttConnection.Connecting -> "Disconnect"
            is RobolabMqttConnection.ConnectionLost -> "Connect"
            is RobolabMqttConnection.Disconnected -> "Connect"
            else -> ""
        }
    }

    fun onStatusAction() {
        when (connection.connectionStateProperty.value) {
            is RobolabMqttConnection.Connected -> {
                connection.disconnect()
            }
            is RobolabMqttConnection.Connecting -> {
                connection.disconnect()
            }
            is RobolabMqttConnection.ConnectionLost -> {
                connection.connect()
            }
            is RobolabMqttConnection.Disconnected -> {
                connection.connect()
            }
        }
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
