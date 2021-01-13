package de.robolab.client.app.model.room

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.repository.Room

class RoomNavigationTab(
    private val messageRepository: MessageRepository,
    private val tabController: TabController,
    private val planetProvider: CachedFilePlanetProvider
) : INavigationBarTab("Group tracked robots by planet name", MaterialIcon.PUBLIC) {

    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        if (entry is RoomNavigationList.Entry) {
            tabController.open(
                RoomPlanetDocument(
                    entry.room,
                    messageRepository,
                    planetProvider
                ), asNewTab
            )
        }
    }

    init {
        activeProperty.value = RoomNavigationList(messageRepository, this)

        messageRepository.onRoomListChange {
            val active = activeProperty.value as? RepositoryEventListener
            active?.onRoomListChange()
        }
        messageRepository.onRoomAttemptListChange { id ->
            val active = activeProperty.value as? RepositoryEventListener
            active?.onRoomAttemptListChange(id)
        }

        messageRepository.onAttemptMessageListChange { id ->
            val active = activeProperty.value as? RepositoryEventListener
            active?.onAttemptMessageListChange(id)
        }
    }

    interface RepositoryEventListener {

        fun onRoomListChange() {}

        fun onRoomAttemptListChange(room: Room) {}

        fun onAttemptMessageListChange(attempt: Attempt) {}
    }

    interface RepositoryList : INavigationBarList, RepositoryEventListener
}
