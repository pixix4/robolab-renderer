package de.robolab.app.model.group

import com.soywiz.klock.DateTime
import de.robolab.app.model.IProvider
import de.robolab.app.model.ISideBarEntry
import de.robolab.communication.From
import de.robolab.communication.MessageManager
import de.robolab.communication.RobolabMessage
import de.westermann.kobserve.Property
import de.westermann.kobserve.list.filterObservable
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortObservable
import de.westermann.kobserve.property.property

class GroupPlanetProvider(
        messageManager: MessageManager
): IProvider {

    val groupList = observableListOf<GroupPlanetEntry>()


    override val searchStringProperty = property("")

    override val entryList = groupList
            .sortObservable(compareBy { it.groupName }).mapObservable { it as ISideBarEntry }

    private fun onMessage(message: RobolabMessage) {
        val group = groupList.find { it.groupName == message.metadata.groupId }
                ?: GroupPlanetEntry(message.metadata.groupId).also { groupList.add(it) }
        group.onMessage(message)
    }

    init {
        messageManager.onMessage += this::onMessage
    }
}
