package de.robolab.client.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.app.model.file.findByName
import de.robolab.client.communication.*
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.mapEvent
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByDescendingObservable
import de.westermann.kobserve.property.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class GroupPlanetEntry(
    val groupName: String,
    val filePlanetProvider: MultiFilePlanetProvider,
    private val messageManager: MessageManager,
    currentTimeProperty: ObservableValue<DateTimeTz>
) : INavigationBarGroup {

    val attempts = observableListOf<AttemptPlanetEntry>()

    val latestAttempt = attempts.mapBinding { it.drop(1).lastOrNull() }
    private val latestPlanetName = latestAttempt.nullableFlatMapBinding { it?.planetNameProperty }
    private val latestMessageTime = latestAttempt.nullableFlatMapBinding { it?.latestMessageTime }
    private val latestMessageTimeDiff = currentTimeProperty.join(latestMessageTime) { now, latest ->
        if (latest != null) {
            val diff = now - latest
            diff.millisecondsLong.toHumanTime()
        } else null
    }

    private fun Long.toHumanTime(): String {
        val minutes = this / 1000 / 60

        if (minutes < 1) {
            return "now"
        } else if (minutes < 60) {
            return "${minutes}min ago"
        }

        val hours = minutes / 60
        if (hours < 24) {
            return "${hours}h ago"
        }

        val days = hours / 24
        if (days == 1L) {
            return "1 day ago"
        } else if (days < 30) {
            return "$days days ago"
        }

        val month = days / 30
        if (month == 1L) {
            return "1 month ago"
        } else if (month < 12) {
            return "$days months ago"
        }

        return "More than 1 year ago"
    }

    override val entryList: ObservableValue<ObservableList<INavigationBarEntry>> = constObservable(
        attempts.sortByDescendingObservable { it.startTime }
    )

    override val titleProperty = latestPlanetName.mapBinding { name ->
        buildString {
            append(groupName)

            if (name != null && name.isNotEmpty()) {
                append(" (")
                append(name)
                append(')')
            }
        }
    }

    override val tabNameProperty = titleProperty.mapBinding { "Group $it" }

    override val subtitleProperty = attempts.join(latestMessageTimeDiff) { attempts, time ->
        val size = attempts.size

        buildString {
            append(size)
            append(" attempt")
            if (size != 1) {
                append("s")
            }

            if (time != null) {
                append(", ")
                append(time)
            }
        }
    }

    override val statusIconProperty = constObservable(emptyList<MaterialIcon>())

    override val parent: INavigationBarGroup? = null

    override val hasContextMenu: Boolean = false

    fun onMessage(message: RobolabMessage, updateAttempt: Boolean = true): AttemptPlanetEntry? {
        if (message is RobolabMessage.TestplanetMessage) {
            return null
        }

        val attempt = if (message is RobolabMessage.ReadyMessage || attempts.isEmpty()) {
            if (attempts.isEmpty()) {
                AttemptPlanetEntry(message.metadata.time + 1, this, messageManager, true).also { attempts.add(it) }
            } else {
                val live = attempts.first()
                live.messages.clear()
                live.startTime = message.metadata.time + 1
            }
            AttemptPlanetEntry(message.metadata.time, this, messageManager).also { attempts.add(it) }
        } else {
            attempts.last()
        }
        val live = attempts.first()

        live.messages.add(message)
        attempt.messages.add(message)

        if (updateAttempt) {
            live.update()
            attempt.update()
        }

        return attempt
    }

    fun onMessage(messageList: List<RobolabMessage>) {
        val changedAttempts = mutableSetOf<AttemptPlanetEntry>()

        for (message in messageList) {
            val attempt = onMessage(message, false)

            if (attempt != null) {
                changedAttempts += attempt
            }
        }

        for (attempt in changedAttempts) {
            attempt.update()
        }
    }
}

class AttemptPlanetEntry(
    var startTime: Long,
    override val parent: GroupPlanetEntry,
    messageManager: MessageManager,
    live: Boolean = false
) : INavigationBarPlottable {

    val messages = observableListOf<RobolabMessage>()

    val latestMessageTime = messages.mapBinding {
        DateTimeTz.fromUnixLocal(it.lastOrNull()?.metadata?.time ?: startTime)
    }

    override val titleProperty = constObservable(
        if (live) {
            "Live"
        } else {
            dateFormat.format(DateTimeTz.Companion.fromUnixLocal(startTime))
        }
    )

    override val tabNameProperty = property(titleProperty, parent.tabNameProperty) {
        "${parent.tabNameProperty.value}: ${titleProperty.value}"
    }

    val attemptNumberProperty = parent.attempts.mapBinding {
        (it.indexOf(this) + 1).toString()
    }

    override val subtitleProperty = messages.mapBinding {
        "Messages: ${it.size}"
    }

    override val hasContextMenu: Boolean = false

    override val toolBarLeft = emptyList<List<ToolBarEntry>>()

    val selectedIndexProperty = property(messages.lastIndex)

    private val canUndoProperty = property(selectedIndexProperty, messages) {
        selectedIndexProperty.value > 0
    }

    fun undo() {
        selectedIndexProperty.value = max(0, selectedIndexProperty.value - 1)
    }

    private val canRedoProperty = property(selectedIndexProperty, messages) {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        selectedIndex < lastIndex
    }

    fun redo() {
        selectedIndexProperty.value = min(messages.lastIndex, selectedIndexProperty.value + 1)
    }

    override val toolBarRight: List<List<ToolBarEntry>> = listOf(
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(
                    MaterialIcon.UNDO
                ), enabledProperty = canUndoProperty
            ) {
                undo()
            },
            ToolBarEntry(
                iconProperty = constObservable(
                    MaterialIcon.REDO
                ), enabledProperty = canRedoProperty
            ) {
                redo()
            }
        )
    )

    override val detailBoxProperty: ObservableValue<IDetailBox> = messages.join(selectedIndexProperty) { _, i ->
        val message = messages.getOrNull(i)
        if (message == null) {
            object : IDetailBox {}
        } else {
            JsonDetailBox(message)
        }
    }

    override val infoBarProperty = constObservable(InfoBarGroupInfo(this, messageManager))

    override val statusIconProperty = constObservable(emptyList<MaterialIcon>())

    override val enabledProperty = constObservable(true)

    val drawable = LivePlanetDrawable()
    override val documentProperty = constObservable(drawable.view)


    val completePlanetNameProperty = messages.mapBinding {
        val message = messages.find { it is RobolabMessage.PlanetMessage } as? RobolabMessage.PlanetMessage
        message?.planetName ?: ""
    }
    val planetNameProperty = property("")
    private val backgroundPlanet = planetNameProperty.nullableFlatMapBinding {
        val entry = parent.filePlanetProvider.findByName(it)
        GlobalScope.launch(Dispatchers.Main) {
            entry?.filePlanet?.load()
        }
        entry?.planetFile?.planetProperty
    }

    private var serverPlanet = Planet.EMPTY
    private var mqttPlanet = Planet.EMPTY
    fun update() {
        val selectedIndex = selectedIndexProperty.value
        val m = if (selectedIndex >= messages.lastIndex) messages else messages.subList(0, selectedIndex + 1)

        serverPlanet = m.toServerPlanet()
        if (planetNameProperty.value != serverPlanet.name) {
            planetNameProperty.value = serverPlanet.name
        }
        mqttPlanet = m.toMqttPlanet()

        if (!isOpen) return

        val planet = backgroundPlanet.value ?: Planet.EMPTY
        drawable.importServerPlanet(serverPlanet.importSplines(planet), true)
        drawable.importMqttPlanet(mqttPlanet.importSplines(planet))
        drawable.importRobot(m.toRobot(parent.groupName.toIntOrNull()))
    }

    private var isOpen = false

    init {
        selectedIndexProperty.onChange { update() }
        messages.onChange {
            if (messages.lastIndex - 1 <= selectedIndexProperty.value) {
                selectedIndexProperty.value = messages.lastIndex
            }
        }

        backgroundPlanet.onChange {
            val planet = backgroundPlanet.value ?: Planet.EMPTY
            drawable.importBackgroundPlanet(planet, true)
            drawable.importServerPlanet(serverPlanet.importSplines(planet), true)
            drawable.importMqttPlanet(mqttPlanet.importSplines(planet))
            drawable.autoCentering = true
            drawable.centerPlanet(TransformationInteraction.ANIMATION_TIME)
        }

        documentProperty.mapEvent { it.onAttach }.addListener {
            isOpen = true
            update()
        }

        documentProperty.mapEvent { it.onDetach }.addListener {
            isOpen = false
        }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
