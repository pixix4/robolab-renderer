package de.robolab.client.app.model.group

import de.robolab.client.app.model.base.IPlanetProvider
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.property

class GroupPlanetProvider(
    private val messageManager: MessageManager,
    private val filePlanetProvider: MultiFilePlanetProvider
) : IPlanetProvider {

    val groupList = observableListOf<GroupPlanetEntry>()


    override val searchStringProperty = property("")

    val sortedGroupList: ObservableList<INavigationBarEntry> = groupList
            .sortByObservable { it.groupName }

    override val entryList = property(sortedGroupList)

    private fun onMessage(message: RobolabMessage) {
        val group = groupList.find { it.groupName == message.metadata.groupId }
                ?: GroupPlanetEntry(message.metadata.groupId, filePlanetProvider, messageManager).also { groupList.add(it) }
        group.onMessage(message)
    }

    private fun onMessage(messageList: List<RobolabMessage>) {
        val groupedMessages = messageList.groupBy { it.metadata.groupId }

        for ((groupId, messages) in groupedMessages) {
            val group = groupList.find { it.groupName == groupId }
                    ?: GroupPlanetEntry(groupId, filePlanetProvider, messageManager).also { groupList.add(it) }
            group.onMessage(messages)
        }
    }

    init {
        messageManager.onMessage += this::onMessage
        messageManager.onMessageList += this::onMessage
    }
}
