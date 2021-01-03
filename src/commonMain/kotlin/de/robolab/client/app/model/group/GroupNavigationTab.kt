package de.robolab.client.app.model.group

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.utils.runAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupNavigationTab(
    private val messageRepository: MessageRepository,
    private val messageManager: MessageManager,
    private val tabController: TabController,
    val planetProvider: CachedFilePlanetProvider
) : INavigationBarTab("MQTT Group list", MaterialIcon.GROUP) {

    override fun submitSearch() {
        val value = searchProperty.value.trim()
        if (childrenProperty.value.isEmpty() && value.isNotEmpty()) {
            GlobalScope.launch {
                messageRepository.createEmptyGroup(value)
                withContext(Dispatchers.Main) {
                    searchProperty.value = ""
                }
            }
        }
    }

    private fun openGroupAttempt(attempt: Attempt, asNewTab: Boolean) {
        tabController.open(
            GroupAttemptPlanetDocument(
                attempt,
                messageRepository,
                messageManager,
                planetProvider
            ), asNewTab
        )
    }

    private fun openGroupLiveAttempt(group: Group, asNewTab: Boolean) {
        GlobalScope.launch {
            val attempt = messageRepository.getLatestAttempt(group.groupId)
            runAsync {
                tabController.open(
                    GroupLiveAttemptPlanetDocument(
                        group,
                        attempt,
                        messageRepository,
                        messageManager,
                        planetProvider
                    ), asNewTab
                )
            }
        }
    }

    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        when (entry) {
            is GroupNavigationList.Entry -> {
                if (asNewTab) {
                    openGroupLiveAttempt(entry.group, false)
                } else {
                    searchProperty.value = ""
                    activeProperty.value = GroupAttemptNavigationList(entry.group, messageRepository, this, activeProperty.value)
                }
            }
            is GroupAttemptNavigationList.Entry -> {
                openGroupAttempt(entry.attempt, asNewTab)
            }
            is GroupAttemptNavigationList.LiveEntry -> {
                openGroupLiveAttempt(entry.group, asNewTab)
            }
        }

    }

    init {
        activeProperty.value = GroupNavigationList(messageRepository, this)

        messageRepository.onGroupListChange {
            val active = activeProperty.value as? RepositoryEventListener
            active?.onGroupListChange()
        }
        messageRepository.onGroupAttemptListChange { id ->
            val active = activeProperty.value as? RepositoryEventListener
            active?.onGroupAttemptListChange(id)
        }

        messageRepository.onAttemptMessageListChange { id ->
            val active = activeProperty.value as? RepositoryEventListener
            active?.onAttemptMessageListChange(id)
        }
    }

    interface RepositoryEventListener {

        fun onGroupListChange() {}

        fun onGroupAttemptListChange(group: Group) {}

        fun onAttemptMessageListChange(attempt: Attempt) {}
    }

    interface RepositoryList : INavigationBarList, RepositoryEventListener
}
