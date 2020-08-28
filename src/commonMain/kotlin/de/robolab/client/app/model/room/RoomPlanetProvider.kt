package de.robolab.client.app.model.room

import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.app.model.file.findByName
import de.robolab.client.app.model.group.AttemptPlanetEntry
import de.robolab.client.app.model.group.GroupPlanetEntry
import de.robolab.client.communication.toRobot
import de.robolab.client.renderer.drawable.planet.MultiRobotPlanetDrawable
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.mapEvent
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.map.observableMapOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
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

    @Suppress("SuspiciousCollectionReassignment")
    fun registerGroup(group: GroupPlanetEntry) {
        val messages = group.latestAttempt.nullableFlatMapBinding { it?.messages }

        messages.onChange.now {
            val currentAttempt = group.latestAttempt.value ?: return@now

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

            // if (lastRoom?.currentGroupAttempts?.isEmpty() == true) {
            //     roomEntryList -= lastRoom
            // }
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

    override val infoBarProperty: ObservableValue<IInfoBarContent> = constObservable(object : IInfoBarContent {})
    override val detailBoxProperty: ObservableValue<IDetailBox> = property(object : IDetailBox {})

    val drawable = MultiRobotPlanetDrawable()
    override val documentProperty = constObservable(drawable.view)

    override val enabledProperty = constObservable(true)
    override val titleProperty = constObservable(planetName)
    override val subtitleProperty = currentGroupAttempts.mapBinding {
        if (it.size == 1) {
            "1 active group"
        } else {
            "${it.size} active groups"
        }
    }
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

        documentProperty.mapEvent { it.onAttach }.addListener {
            isOpen = true
            update()
        }

        documentProperty.mapEvent { it.onDetach }.addListener {
            isOpen = false
        }
    }
}
