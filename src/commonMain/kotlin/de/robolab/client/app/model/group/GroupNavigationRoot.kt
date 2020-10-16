package de.robolab.client.app.model.group

import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.INavigationBarEntryRoot
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupNavigationRoot(
    private val messageRepository: MessageRepository,
    private val messageManager: MessageManager,
    private val tabController: TabController,
    val planetProvider: CachedFilePlanetProvider
) : INavigationBarEntryRoot, NavigationBarController.Tab {

    override val label = property("MQTT Group list")
    override val icon = property(MaterialIcon.GROUP)

    override val searchProperty = property("")

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

    private val activeListProperty = property<RepositoryList>(GroupNavigationList(messageRepository, this))
    private val activeList by activeListProperty

    override val childrenProperty = searchProperty.join(activeListProperty) { search, active ->
        if (search.isEmpty()) {
            active.childrenProperty
        } else {
            active.childrenProperty.filter {
                it.nameProperty.value.contains(search, true)
            }.toMutableList().asObservable()
        }
    }

    override val parentNameProperty = activeListProperty.flatMapBinding {
        it.parentNameProperty
    }

    override fun openParent() {
        searchProperty.value = ""
        activeList.openParent()
    }

    fun openGroupList() {
        searchProperty.value = ""
        activeListProperty.value = GroupNavigationList(messageRepository, this)
    }

    fun openGroupAttemptList(group: Group) {
        searchProperty.value = ""
        activeListProperty.value = GroupAttemptNavigationList(group, messageRepository, this)
    }

    fun openGroupAttempt(attempt: Attempt, asNewTab: Boolean) {
        tabController.open(
            GroupAttemptPlanetDocument(
                attempt,
                messageRepository,
                messageManager,
                planetProvider
            ), asNewTab
        )
    }

    fun openGroupLiveAttempt(group: Group, asNewTab: Boolean) {
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

    init {

        messageRepository.onGroupListChange {
            activeList.onGroupListChange()
        }
        messageRepository.onGroupAttemptListChange { id ->
            activeList.onGroupAttemptListChange(id)
        }

        messageRepository.onAttemptMessageListChange { id ->
            activeList.onAttemptMessageListChange(id)
        }
    }

    interface RepositoryEventListener {

        fun onGroupListChange() {}

        fun onGroupAttemptListChange(group: Group) {}

        fun onAttemptMessageListChange(attempt: Attempt) {}
    }

    interface RepositoryList : INavigationBarList, RepositoryEventListener
}
