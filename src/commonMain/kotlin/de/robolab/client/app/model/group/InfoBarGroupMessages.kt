package de.robolab.client.app.model.group

import de.robolab.client.app.controller.DialogController
import de.robolab.client.app.controller.dialog.SendMessageDialogController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.app.viewmodel.dialog.SendMessageDialogViewModel
import de.robolab.client.communication.From
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessage
import de.robolab.common.utils.formatDateTime
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import kotlinx.datetime.Instant
import kotlin.math.roundToInt

class InfoBarGroupMessages(
    private val attemptProperty: ObservableValue<Attempt>,
    val messages: ObservableList<RobolabMessage>,
    val selectedIndexProperty: ObservableProperty<Int>,
    private val planetNameProperty: ObservableValue<String>,
    private val messageManager: MessageManager,
    private val undoListener: () -> Unit,
    private val redoListener: () -> Unit,
    val uiController: UiController
) : SideBarTabViewModel("Messages", MaterialIcon.INFO_OUTLINE), SideBarContentViewModel {

    override val parent: SideBarContentViewModel? = null

    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)
    override val topToolBar = buildFormContent {  }
    override val bottomToolBar = buildFormContent {  }

    private val robolabMessageProperty = selectedIndexProperty.mapBinding {
        messages.getOrNull(it)
    }

    fun undo() {
        undoListener()
    }

    fun redo() {
        redoListener()
    }

    fun openSendDialog() {
        val controller = SendMessageDialogController(
            attemptProperty.value.groupName,
            planetNameProperty.value,
            messageManager
        )

        val dialog = SendMessageDialogViewModel(controller)
        DialogController.open(dialog)
    }

    fun openSendDialogExamPlanet(planetName: String) {
        val controller = SendMessageDialogController(
            attemptProperty.value.groupName,
            planetNameProperty.value,
            messageManager
        )

        controller.topicControllerGroup()
        controller.type = SendMessageDialogController.Type.ControllerSetPlanetMessage
        controller.from = SendMessageDialogController.From.CLIENT
        controller.planetName = planetName

        val dialog = SendMessageDialogViewModel(controller)
        DialogController.open(dialog)
    }

    val messageCountStringProperty = messages.join(selectedIndexProperty) { messages, index ->
        "${messages.size} (${if (index < messages.size - 1) "${index + 1} of ${messages.size}" else "live"})"
    }

    val firstMessageTimeStringProperty = messages.mapBinding { list ->
        list.firstOrNull()?.metadata?.time?.let {
            formatDateTime(Instant.fromEpochMilliseconds(it), TIME_FORMAT)
        } ?: ""
    }

    val lastMessageTimeStringProperty = messages.mapBinding { list ->
        list.lastOrNull()?.metadata?.time?.let {
            formatDateTime(Instant.fromEpochMilliseconds(it), TIME_FORMAT)
        } ?: ""
    }

    val attemptDurationStringProperty = messages.mapBinding { list ->
        list.getDuration()
    }

    val headerProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else
            it::class.simpleName ?: "Information"
    }

    val fromProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else
            it.metadata.from.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
    val fromEnumProperty = robolabMessageProperty.mapBinding {
        it?.metadata?.from ?: From.UNKNOWN
    }
    val groupProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else
            it.metadata.groupId
    }
    val topicProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else
            it.metadata.topic
    }
    val timeProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else formatDateTime(Instant.fromEpochMilliseconds(it.metadata.time), TIME_FORMAT_DETAILED)
    }

    val detailsProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else
            it.details.joinToString("\n") { (key, value) ->
                "$key: $value"
            }
    }

    val rawMessageProperty = robolabMessageProperty.mapBinding {
        if (it == null) "" else
            formatRawMessage(it)
    }

    companion object {
        const val TIME_FORMAT = "HH:mm:ss"
        const val TIME_FORMAT_DETAILED = "HH:mm:ss.SSS"

        private fun formatRawMessage(message: RobolabMessage): String {
            val rawMessage = message.metadata.rawMessage

            val builder = StringBuilder()

            var depth = 0
            var isString = false
            var stringChar = '"'
            var isEscape = false

            fun appendNewLine() {
                builder.append('\n')
                builder.append(" ".repeat(2 * depth))
            }

            for (char in rawMessage) {
                var isNextEscape = false
                when (char) {
                    '{', '[' -> {
                        builder.append(char)
                        if (!isString) {
                            depth += 1
                            appendNewLine()
                        }
                    }
                    '}', ']' -> {
                        if (!isString) {
                            depth -= 1
                            appendNewLine()
                        }
                        builder.append(char)
                    }
                    ',' -> {
                        builder.append(',')
                        if (!isString) {
                            appendNewLine()
                        }
                    }
                    ':' -> {
                        builder.append(':')
                        if (!isString) {
                            builder.append(' ')
                        }
                    }
                    ' ' -> {
                        if (isString) {
                            builder.append(' ')
                        }
                    }
                    '\n' -> {
                    }
                    '\\' -> {
                        if (isEscape) {
                            builder.append('\\')
                        } else {
                            isNextEscape = true
                        }
                    }
                    '\'' -> {
                        builder.append('\'')
                        if (!isString) {
                            isString = true
                            stringChar = '\''
                        } else {
                            if (stringChar == '\'' && !isEscape) {
                                isString = false
                            }
                        }
                    }
                    '"' -> {
                        builder.append('"')
                        if (!isString) {
                            isString = true
                            stringChar = '"'
                        } else {
                            if (stringChar == '"' && !isEscape) {
                                isString = false
                            }
                        }
                    }
                    else -> {
                        builder.append(char)
                    }
                }

                isEscape = isNextEscape
            }

            return builder.toString()
        }
    }
}

fun Collection<RobolabMessage>.getDuration(): String {
    val first = firstOrNull()?.metadata?.time ?: return ""
    val last = lastOrNull()?.metadata?.time ?: return ""
    val diff = last - first

    val roundedSeconds = (diff / 1000.0).roundToInt()
    return "${roundedSeconds / 60}:${(roundedSeconds % 60).toString().padStart(2, '0')}"
}
