package de.robolab.app.model.group

import de.robolab.app.model.IProvider
import de.robolab.app.model.ISideBarEntry
import de.robolab.app.model.file.FilePlanetProvider
import de.robolab.communication.MessageManager
import de.robolab.communication.RobolabMessage
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortObservable
import de.westermann.kobserve.property.property

class GroupPlanetProvider(
        messageManager: MessageManager,
        private val filePlanetProvider: FilePlanetProvider
): IProvider {

    val groupList = observableListOf<GroupPlanetEntry>()


    override val searchStringProperty = property("")

    override val entryList = groupList
            .sortObservable(compareBy { it.groupName }).mapObservable { it as ISideBarEntry }

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
