package de.robolab.client.app.model.group

import de.robolab.client.app.model.IProvider
import de.robolab.client.app.model.INavigationBarEntry
import de.robolab.client.app.model.file.FilePlanetProvider
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.property

class GroupPlanetProvider(
    messageManager: MessageManager,
    private val filePlanetProvider: FilePlanetProvider
) : IProvider {

    val groupList = observableListOf<GroupPlanetEntry>()


    override val searchStringProperty = property("")

    override val entryList: ObservableList<INavigationBarEntry> = groupList
            .sortByObservable { it.groupName }

    private fun onMessage(message: RobolabMessage) {
        val group = groupList.find { it.groupName == message.metadata.groupId }
                ?: GroupPlanetEntry(message.metadata.groupId, filePlanetProvider).also { groupList.add(it) }
        group.onMessage(message)
    }

    private fun onMessage(messageList: List<RobolabMessage>) {
        val groupedMessages = messageList.groupBy { it.metadata.groupId }

        for ((groupId, messages) in groupedMessages) {
            val group = groupList.find { it.groupName == groupId }
                    ?: GroupPlanetEntry(groupId, filePlanetProvider).also { groupList.add(it) }
            group.onMessage(messages)
        }
    }

    init {
        messageManager.onMessage += this::onMessage
        messageManager.onMessageList += this::onMessage
    }
}
