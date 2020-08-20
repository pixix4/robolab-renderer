package de.robolab.client.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.format
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.file.openSendMessageDialog
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import kotlin.math.roundToInt

class InfoBarGroupInfo(
    private val attemptPlanetEntry: AttemptPlanetEntry, private val messageManager: MessageManager
) : IInfoBarContent {

    override val nameProperty = constObservable("Overview")

    val messages: ObservableList<RobolabMessage> = attemptPlanetEntry.messages

    val selectedIndexProperty = attemptPlanetEntry.selectedIndexProperty

    fun undo() {
        attemptPlanetEntry.undo()
    }

    fun redo() {
        attemptPlanetEntry.redo()
    }

    fun openSendDialog() {
        openSendMessageDialog("explorer/" + attemptPlanetEntry.parent.groupName, messageManager::sendMessage)
    }

    val messageCountStringProperty = messages.join(attemptPlanetEntry.selectedIndexProperty) { messages, index ->
        "${messages.size} (${if (index < messages.size - 1) "${index + 1} of ${messages.size}" else "live"})"
    }

    val firstMessageTimeStringProperty = messages.mapBinding { list ->
        list.firstOrNull()?.metadata?.time?.let {
            TIME_FORMAT.format(it)
        } ?: ""
    }

    val lastMessageTimeStringProperty = messages.mapBinding { list ->
        list.lastOrNull()?.metadata?.time?.let {
            TIME_FORMAT.format(it)
        } ?: ""
    }

    val attemptDurationStringProperty = messages.mapBinding { list ->
        val first = list.firstOrNull()?.metadata?.time ?: return@mapBinding ""
        val last = list.lastOrNull()?.metadata?.time ?: return@mapBinding ""
        val diff = last - first

        val roundedSeconds = (diff / 1000.0).roundToInt()
        "${roundedSeconds / 60}:${(roundedSeconds % 60).toString().padStart(2, '0')}"
    }

    companion object {
        val TIME_FORMAT = DateFormat("HH:mm:ss")
        val TIME_FORMAT_DETAILED = DateFormat("HH:mm:ss.SSS")
    }
}
