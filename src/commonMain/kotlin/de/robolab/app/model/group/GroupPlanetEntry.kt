package de.robolab.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.app.model.ISideBarEntry
import de.robolab.app.model.ISideBarPlottable
import de.robolab.app.model.ISideBarGroup
import de.robolab.communication.RobolabMessage
import de.robolab.communication.toMqttPlanet
import de.robolab.communication.toServerPlanet
import de.robolab.renderer.drawable.planet.LivePlanetDrawable
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

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

    override val actionList = emptyList<List<ISideBarPlottable.PlottableAction>>()
    override val unsavedChangesProperty = constProperty(false)

    override val enabledProperty = constProperty(false)

    override val drawable = LivePlanetDrawable()

    val maximumIndexProperty = property<Int?>(null)

    private fun update() {
        println("Render ${messages.size} messages")
        println(messages)
        drawable.importServerPlanet(messages.toServerPlanet(maximumIndexProperty.value))
        drawable.importMqttPlanet(messages.toMqttPlanet(maximumIndexProperty.value))
    }

    init {
        messages.onChange { update() }
        maximumIndexProperty.onChange { update() }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
