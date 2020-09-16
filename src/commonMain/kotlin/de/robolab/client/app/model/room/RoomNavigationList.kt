package de.robolab.client.app.model.room

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.DatabaseMessageStorage
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.repository.Room
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.buildContextMenu
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.Point
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomNavigationList(
    private val messageRepository: MessageRepository,
    private val root: RoomNavigationRoot
) : RoomNavigationRoot.RepositoryList {

    override val parentNameProperty = constObservable<String?>(null)

    override fun openParent() {
        throw IllegalStateException()
    }

    override val childrenProperty = observableListOf<Entry>()

    override fun onRoomListChange() {
        GlobalScope.launch {
            val list = messageRepository
                .getRoomList()
                .map {
                    Entry(it)
                }

            runAsync {
                childrenProperty.sync(list)
            }
        }
    }

    override fun onRoomAttemptListChange(room: Room) {
        childrenProperty.find { it.room.roomId == room.roomId }?.update(room)
    }

    init {
        GlobalScope.launch {
            val list = messageRepository
                .getRoomList()
                .map {
                    Entry(it)
                }

            runAsync {
                childrenProperty.addAll(list)
            }
        }
    }

    inner class Entry(
        room: Room
    ) : INavigationBarEntry {

        private val roomProperty = property(room)
        val room by roomProperty

        fun update(room: Room) {
            roomProperty.value = room
        }

        override val nameProperty = roomProperty.mapBinding { room ->
            room.name
        }

        override val subtitleProperty = roomProperty.mapBinding { room ->
            buildString {
                append(room.groupCount)
                append(" active group")
                if (room.groupCount != 1) {
                    append("s")
                }
            }
        }

        override val enabledProperty = constObservable(true)

        override val statusIconProperty = constObservable<List<MaterialIcon>>(emptyList())

        override fun contextMenu(position: Point): ContextMenu? {
            return buildContextMenu(position, nameProperty.value) {
                action("Open in new tab") {
                    open(true)
                }
            }
        }

        override fun open(asNewTab: Boolean) {
            root.openRoom(room, asNewTab)
        }
    }
}