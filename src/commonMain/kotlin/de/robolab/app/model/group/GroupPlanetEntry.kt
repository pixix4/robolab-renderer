package de.robolab.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.app.model.*
import de.robolab.app.model.file.FilePlanetProvider
import de.robolab.app.model.file.findByName
import de.robolab.communication.RobolabMessage
import de.robolab.communication.toMqttPlanet
import de.robolab.communication.toRobot
import de.robolab.communication.toServerPlanet
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.utils.Logger
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min

class GroupPlanetEntry(val groupName: String, val filePlanetProvider: FilePlanetProvider) : ISideBarGroup {

    val attempts = observableListOf<AttemptPlanetEntry>()

    override val entryList: ObservableList<ISideBarEntry> = attempts.mapObservable { it as ISideBarEntry }

    override val titleProperty = constObservable(groupName)

    override val tabNameProperty = titleProperty.mapBinding { "Group $it" }

    override val subtitleProperty = attempts.mapBinding {
        "Attempts: ${it.size}"
    }

    override val unsavedChangesProperty = constObservable(false)

    override val parent: ISideBarGroup? = null

    override val hasContextMenu: Boolean = false

    fun onMessage(message: RobolabMessage, updateAttempt: Boolean = true): AttemptPlanetEntry? {
        if (message is RobolabMessage.TestplanetMessage) {
            return null
        }

        val attempt = if (message is RobolabMessage.ReadyMessage || attempts.isEmpty()) {
            val attempt = AttemptPlanetEntry(message.metadata.time, this)
            attempt.messages.add(message)
            attempts.add(attempt)
            attempt
        } else {
            val attempt = attempts.last()
            attempt.messages.add(message)
            attempt
        }

        if (updateAttempt) {
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

class AttemptPlanetEntry(val startTime: Long, override val parent: GroupPlanetEntry) : ISideBarPlottable {

    private val logger = Logger(this)

    val messages = observableListOf<RobolabMessage>()

    override val titleProperty = parent.attempts.mapBinding {
        dateFormat.format(DateTimeTz.Companion.fromUnixLocal(startTime))
    }

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

    val selectedIndexProperty = property<Int?>(null)

    private val canUndoProperty = property(selectedIndexProperty, messages) {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        if (selectedIndex == null) {
            lastIndex > 0
        } else {
            selectedIndex > 0
        }
    }

    private fun undo() {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        if (selectedIndex == null) {
            selectedIndexProperty.value = max(0, lastIndex - 1)
        } else {
            selectedIndexProperty.value = max(0, selectedIndex - 1)
        }
    }

    private val canRedoProperty = property(selectedIndexProperty, messages) {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        selectedIndex != null && selectedIndex < lastIndex
    }

    private fun redo() {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        if (selectedIndex != null) {
            if (selectedIndex >= lastIndex) {
                selectedIndexProperty.value = null
            } else {
                selectedIndexProperty.value = min(lastIndex, selectedIndex + 1)
            }
        }
    }

    override val toolBarRight: List<List<ToolBarEntry>> = listOf(
            listOf(
                    ToolBarEntry(iconProperty = constObservable(ToolBarEntry.Icon.UNDO), enabledProperty = canUndoProperty) {
                        undo()
                    },
                    ToolBarEntry(iconProperty = constObservable(ToolBarEntry.Icon.REDO), enabledProperty = canRedoProperty) {
                        redo()
                    }
            )
    )

    override val infoBarList: List<IInfoBarContent> = listOf(InfoBarGroupInfo(this))
    override val selectedInfoBarIndexProperty = property<Int?>(0)

    override val unsavedChangesProperty = constObservable(false)

    override val enabledProperty = constObservable(false)

    override val drawable = LivePlanetDrawable()


    private val planetNameProperty = property("")
    private val backgroundPlanet = planetNameProperty.nullableFlatMapBinding {
        val entry = parent.filePlanetProvider.findByName(it)
        entry?.onOpen()
        entry?.planetFile?.planetProperty
    }.mapBinding { it ?: Planet.EMPTY }

    private var serverPlanet = Planet.EMPTY
    private var mqttPlanet = Planet.EMPTY
    fun update() {
        val selectedIndex = selectedIndexProperty.value
        val m = if (selectedIndex == null) messages else messages.subList(0, selectedIndex - 1)

        serverPlanet = m.toServerPlanet()
        planetNameProperty.value = serverPlanet.name
        mqttPlanet = m.toMqttPlanet()

        drawable.importServerPlanet(serverPlanet.importSplines(backgroundPlanet.value))
        drawable.importMqttPlanet(mqttPlanet.importSplines(backgroundPlanet.value))
        drawable.importRobot(m.toRobot(parent.groupName.toIntOrNull()))
    }

    init {
        selectedIndexProperty.onChange { update() }

        backgroundPlanet.onChange {
            drawable.importBackgroundPlanet(backgroundPlanet.value)
            drawable.importServerPlanet(serverPlanet.importSplines(backgroundPlanet.value))
            drawable.importMqttPlanet(mqttPlanet.importSplines(backgroundPlanet.value))
        }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
