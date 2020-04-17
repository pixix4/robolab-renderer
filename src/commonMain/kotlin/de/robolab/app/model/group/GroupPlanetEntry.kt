package de.robolab.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.app.model.*
import de.robolab.communication.RobolabMessage
import de.robolab.communication.toMqttPlanet
import de.robolab.communication.toServerPlanet
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.utils.ContextMenu
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min

class GroupPlanetEntry(val groupName: String) : ISideBarGroup {

    val attempts = observableListOf<AttemptPlanetEntry>()

    override val entryList: ObservableReadOnlyList<ISideBarEntry> = attempts.mapObservable { it as ISideBarEntry }

    override val titleProperty = constProperty(groupName)

    override val tabNameProperty = titleProperty.mapBinding { "Group $it" }

    override val subtitleProperty = attempts.mapBinding {
        "Attempts: ${it.size}"
    }

    override val unsavedChangesProperty = constProperty(false)

    override val parent: ISideBarGroup? = null

    override val hasContextMenu: Boolean = false

    fun onMessage(message: RobolabMessage) {
        if (message is RobolabMessage.TestplanetMessage) {
            return
        }

        if (message is RobolabMessage.ReadyMessage || attempts.isEmpty()) {
            attempts.add(AttemptPlanetEntry(message.metadata.time, this).apply { messages.add(message) })
        } else {
            attempts.last().messages.add(message)
        }
    }
}

class AttemptPlanetEntry(val startTime: Long, override val parent: GroupPlanetEntry) : ISideBarPlottable {

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
                    ToolBarEntry(iconProperty = constProperty(ToolBarEntry.Icon.UNDO), enabledProperty = canUndoProperty) {
                        undo()
                    },
                    ToolBarEntry(iconProperty = constProperty(ToolBarEntry.Icon.REDO), enabledProperty =canRedoProperty) {
                        redo()
                    }
            )
    )

    override val infoBarList: List<IInfoBarContent> = emptyList()
    override val selectedInfoBarIndexProperty = property<Int?>(null)

    override val unsavedChangesProperty = constProperty(false)

    override val enabledProperty = constProperty(false)

    override val drawable = LivePlanetDrawable()


    private fun update() {
        val selectedIndex = selectedIndexProperty.value
        val m = if (selectedIndex == null) messages else messages.subList(0, selectedIndex - 1)

        println("Render ${m.size} messages")

        drawable.importServerPlanet(m.toServerPlanet())
        drawable.importMqttPlanet(m.toMqttPlanet())
    }

    init {
        messages.onChange { update() }
        selectedIndexProperty.onChange { update() }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
