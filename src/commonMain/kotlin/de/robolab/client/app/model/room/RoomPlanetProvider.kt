package de.robolab.client.app.model.room

import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.app.model.file.findByName
import de.robolab.client.app.model.group.AttemptPlanetEntry
import de.robolab.client.app.model.group.GroupPlanetEntry
import de.robolab.client.communication.toRobot
import de.robolab.client.renderer.drawable.planet.MultiRobotPlanetDrawable
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.map.observableMapOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomPlanetProvider(
    groupList: ObservableMutableList<GroupPlanetEntry>,
    private val filePlanetProvider: MultiFilePlanetProvider
) : IPlanetProvider {

    override val searchStringProperty =
        property("")

    private val roomEntryList = observableListOf<INavigationBarEntry>()
    override val entryList = property(roomEntryList)

    private var nameToRoomMap = emptyMap<String, RoomPlanetEntry>()
    private var groupToRoomMap = emptyMap<GroupPlanetEntry, RoomPlanetEntry>()
    private var attemptToListenerMap = emptyMap<GroupPlanetEntry, EventListener<*>>()

    @Suppress("SuspiciousCollectionReassignment")
    fun registerGroup(group: GroupPlanetEntry) {
        group.attempts.onChange.now {
            attemptToListenerMap[group]?.detach()

            val currentAttempt = group.attempts.lastOrNull() ?: return@now

            val listener = currentAttempt.messages.onChange.reference {
                val planetName = currentAttempt.completePlanetNameProperty.value
                val lastRoom = groupToRoomMap[group]

                if (planetName.isNotEmpty()) {
                    val nextRoom = nameToRoomMap[planetName]
                        ?: RoomPlanetEntry(planetName, filePlanetProvider).also {
                            nameToRoomMap += planetName to it
                            roomEntryList += it
                        }
                    if (lastRoom == nextRoom) {
                        nextRoom.updateGroup(group, currentAttempt)
                    } else {
                        lastRoom?.removeGroup(group)
                        nextRoom.addGroup(group, currentAttempt)
                        groupToRoomMap += group to nextRoom
                    }
                } else {
                    lastRoom?.removeGroup(group)
                    groupToRoomMap -= group
                }

                if (lastRoom?.currentGroupAttempts?.isEmpty() == true) {
                    roomEntryList -= lastRoom
                }
            }

            attemptToListenerMap += group to listener
            listener.emit(Unit)
        }
    }

    init {
        groupList.onAdd { group ->
            registerGroup(group)
        }
    }
}

class RoomPlanetEntry(planetName: String, filePlanetProvider: MultiFilePlanetProvider) : INavigationBarPlottable {

    val currentGroupAttempts = observableMapOf<GroupPlanetEntry, AttemptPlanetEntry>()

    override val toolBarLeft = emptyList<List<ToolBarEntry>>()
    override val toolBarRight = emptyList<List<ToolBarEntry>>()
    override val infoBarList = emptyList<IInfoBarContent>()

    override val detailBoxProperty: ObservableValue<IDetailBox> = property(object : IDetailBox {})
    override val selectedInfoBarIndexProperty: ObservableProperty<Int?> = property()

    val drawable = MultiRobotPlanetDrawable()
    override val document = drawable.view

    override val enabledProperty = constObservable(true)
    override val titleProperty = constObservable(planetName)
    override val subtitleProperty = currentGroupAttempts.mapBinding { "Groups: ${it.size}" }
    override val tabNameProperty = titleProperty
    override val hasContextMenu = false
    override val statusIconProperty = constObservable(emptyList<MaterialIcon>())
    override val parent: INavigationBarGroup? = null

    private var isOpen = false

    fun addGroup(group: GroupPlanetEntry, attempt: AttemptPlanetEntry) {
        currentGroupAttempts += group to attempt
    }

    fun updateGroup(group: GroupPlanetEntry, attempt: AttemptPlanetEntry) {
        currentGroupAttempts[group] = attempt
        update()
    }

    fun removeGroup(group: GroupPlanetEntry) {
        currentGroupAttempts -= group
    }

    fun update() {
        if (!isOpen) return

        val robots = currentGroupAttempts.mapNotNull { (group, attempt) ->
            attempt.messages.toRobot(group.groupName.toIntOrNull() ?: 0)
        }
        drawable.importRobots(robots)
    }

    init {
        val entry = filePlanetProvider.findByName(planetName)
        if (entry != null) {
            GlobalScope.launch(Dispatchers.Main) {
                entry.filePlanet.load()
                drawable.importPlanet(entry.planetFile.planet)
            }
        }

        currentGroupAttempts.onChange {
            update()
        }

        document.onAttach {
            isOpen = true
            update()
        }

        document.onDetach {
            isOpen = false
        }
    }
}
