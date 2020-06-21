package de.robolab.client.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.MultiFilePlanetProvider
import de.robolab.client.app.model.file.findByName
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.communication.toMqttPlanet
import de.robolab.client.communication.toRobot
import de.robolab.client.communication.toServerPlanet
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class GroupPlanetEntry(val groupName: String, val filePlanetProvider: MultiFilePlanetProvider) :
    INavigationBarGroup {

    val attempts = observableListOf<AttemptPlanetEntry>()

    override val entryList: ObservableValue<ObservableList<INavigationBarEntry>> = constObservable(attempts)

    override val titleProperty = constObservable(groupName)

    override val tabNameProperty = titleProperty.mapBinding { "Group $it" }

    override val subtitleProperty = attempts.mapBinding {
        "Attempts: ${it.size}"
    }

    override val statusIconProperty = constObservable(emptyList<MaterialIcon>())

    override val parent: INavigationBarGroup? = null

    override val hasContextMenu: Boolean = false

    fun onMessage(message: RobolabMessage, updateAttempt: Boolean = true): AttemptPlanetEntry? {
        if (message is RobolabMessage.TestplanetMessage) {
            return null
        }

        val attempt = if (message is RobolabMessage.ReadyMessage || attempts.isEmpty()) {
            AttemptPlanetEntry(message.metadata.time, this).also { attempts.add(it) }
        } else {
            attempts.last()
        }
        attempt.messages.add(message)

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

class AttemptPlanetEntry(val startTime: Long, override val parent: GroupPlanetEntry) :
    INavigationBarPlottable {

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

    override val infoBarList: List<IInfoBarContent> = listOf(InfoBarGroupInfo(this))
    override val selectedInfoBarIndexProperty = property<Int?>(0)

    override val statusIconProperty = constObservable(emptyList<MaterialIcon>())

    override val enabledProperty = constObservable(true)

    val drawable = LivePlanetDrawable()
    override val document = drawable.view


    private val planetNameProperty = property("")
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
        if (!isOpen) return

        val selectedIndex = selectedIndexProperty.value
        val m = if (selectedIndex >= messages.lastIndex) messages else messages.subList(0, selectedIndex + 1)

        serverPlanet = m.toServerPlanet()
        if (planetNameProperty.value != serverPlanet.name) {
            planetNameProperty.value = serverPlanet.name
        }
        mqttPlanet = m.toMqttPlanet()

        val planet = backgroundPlanet.value ?: Planet.EMPTY
        drawable.importServerPlanet(serverPlanet.importSplines(planet), true)
        drawable.importMqttPlanet(mqttPlanet.importSplines(planet))
        drawable.importRobot(m.toRobot(parent.groupName.toIntOrNull()))
    }

    var isOpen = false

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
        }

        document.onAttach {
            isOpen = true
            update()
        }

        document.onDetach {
            isOpen = false
        }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
