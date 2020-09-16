package de.robolab.client.app.model.room

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.INavigationBarEntryRoot
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.repository.Room
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class RoomNavigationRoot(
    private val messageRepository: MessageRepository,
    private val tabController: TabController,
    private val planetProvider: CachedFilePlanetProvider
) : INavigationBarEntryRoot {

    override val searchProperty = property("")

    private val activeListProperty = property<RepositoryList>(RoomNavigationList(messageRepository, this))
    private val activeList by activeListProperty

    override val parentNameProperty = activeListProperty.flatMapBinding {
        it.parentNameProperty
    }

    override fun openParent() {
        activeList.openParent()
    }

    override val childrenProperty = activeListProperty.mapBinding { it.childrenProperty }

    fun openRoomList() {
        activeListProperty.value = RoomNavigationList(messageRepository, this)
    }

    fun openRoom(room: Room, asNewTab: Boolean) {
        tabController.open(
            RoomPlanetDocument(
                room,
                messageRepository,
                planetProvider
            ), asNewTab
        )
    }

    init {

        messageRepository.onRoomListChange {
            activeList.onRoomListChange()
        }
        messageRepository.onRoomAttemptListChange { id ->
            activeList.onRoomAttemptListChange(id)
        }

        messageRepository.onAttemptMessageListChange { id ->
            activeList.onAttemptMessageListChange(id)
        }
    }

    interface RepositoryEventListener {

        fun onRoomListChange() {}

        fun onRoomAttemptListChange(room: Room) {}

        fun onAttemptMessageListChange(attempt: Attempt) {}
    }

    interface RepositoryList : INavigationBarList, RepositoryEventListener
}
