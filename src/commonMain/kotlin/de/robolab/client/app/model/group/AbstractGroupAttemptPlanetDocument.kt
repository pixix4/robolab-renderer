package de.robolab.client.app.model.group

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.repository.Attempt
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.communication.toMqttPlanet
import de.robolab.client.communication.toRobot
import de.robolab.client.communication.toServerPlanet
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.common.planet.Planet
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min

abstract class AbstractGroupAttemptPlanetDocument : IPlanetDocument {

    abstract val attempt: Attempt

    private val messages = observableListOf<RobolabMessage>()

    val selectedIndexProperty = property(messages.lastIndex)

    override val canUndoProperty = property(selectedIndexProperty, messages) {
        selectedIndexProperty.value > 0
    }

    override fun undo() {
        selectedIndexProperty.value = max(0, selectedIndexProperty.value - 1)
    }

    override val canRedoProperty = property(selectedIndexProperty, messages) {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        selectedIndex < lastIndex
    }

    override fun redo() {
        selectedIndexProperty.value = min(messages.lastIndex, selectedIndexProperty.value + 1)
    }

    val drawable = LivePlanetDrawable()
    override val documentProperty = constObservable(drawable.view)


    val planetNameProperty = property("")

    protected val backgroundPlanet = property<Planet>()
    protected var serverPlanet = Planet.EMPTY
    protected var mqttPlanet = Planet.EMPTY

    private var lastSelectedIndex = selectedIndexProperty.value
    fun update() {
        val selectedIndex = selectedIndexProperty.value
        val m = if (selectedIndex >= messages.lastIndex) messages else messages.take(selectedIndex + 1)

        val (sp, visitedPoints) = m.toServerPlanet()
        serverPlanet = sp
        if (planetNameProperty.value != serverPlanet.name) {
            planetNameProperty.value = serverPlanet.name
        }
        mqttPlanet = m.toMqttPlanet()

        if (!isAttached) return

        val planet = backgroundPlanet.value ?: Planet.EMPTY
        drawable.importServerPlanet(
            serverPlanet.importSplines(planet).importSenderGroups(planet, visitedPoints),
            true
        )
        drawable.importMqttPlanet(mqttPlanet.importSplines(planet))

        val backward = selectedIndex < lastSelectedIndex
        lastSelectedIndex = selectedIndex
        drawable.importRobot(m.toRobot(attempt.groupName.toIntOrNull(), backward))
    }

    abstract val isAttached: Boolean
}
