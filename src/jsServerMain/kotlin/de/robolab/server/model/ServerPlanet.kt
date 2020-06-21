package de.robolab.server.model

import com.soywiz.klock.DateTime
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.planet.randomName
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.observe
import de.westermann.kobserve.property.property

class ServerPlanet(info: ServerPlanetInfo, lines: List<String> = listOf("#name: ${info.name}")) {


    companion object {
        fun random(): ServerPlanet = Template.random().let { it.withID(it.name) }
    }

    val planetFile: PlanetFile = PlanetFile(lines.joinToString("\n"))

    private val _fallbackName: ObservableProperty<String> = info.name.observe()

    val nameProp: ObservableValue<String> = property(planetFile.planetProperty, _fallbackName) {
        if (planetFile.planet.name.isEmpty()) _fallbackName.value else planetFile.planet.name
    }
    val name: String by nameProp

    val linesProp: ObservableValue<List<String>> =
        property(planetFile.planetProperty) {
            if (planetFile.content.isEmpty())
                emptyList()
            else
                planetFile.content
        }
    val lines: List<String> by linesProp

    val id: String = info.id

    val _lastModified: ObservableProperty<DateTime> = info.lastModifiedDate.observe()
    val lastModifiedProp: ObservableValue<DateTime> = _lastModified
    val lastModified: DateTime by lastModifiedProp

    val infoProp: ObservableValue<ServerPlanetInfo> = property(nameProp, lastModifiedProp) {
        ServerPlanetInfo(id, name, lastModified)
    }
    val info by infoProp

    init {
        this.planetFile.planetProperty.onChange.addListener {
            _lastModified.set(DateTime.now())
        }
        var previousModifiedInfo = this.info
        this.infoProp.onChange.addListener {
            val newValue = this.info
            if (previousModifiedInfo.withMTime(newValue.lastModifiedDate) != newValue) {
                previousModifiedInfo = newValue
                _lastModified.set(DateTime.now())
            }
        }
        this.nameProp.onChange.addListener {
            if (this.name != this._fallbackName.value)
                this._fallbackName.set(this.name)
        }
    }

    fun asTemplate(): Template = Template(name, planetFile.content)

    suspend fun lockLines(): Unit {}
    suspend fun lockLines(timeout: Int) {}
    fun unlockLines(): Unit {}

    class Template(val name: String, lines: List<String> = listOf("#name: $name")) {
        val lines: MutableList<String> = lines.toMutableList()

        fun withID(id: String): ServerPlanet = ServerPlanet(ServerPlanetInfo(id, name, DateTime.now()), lines = lines)

        fun reparsed(): Template = fromLines(lines, name)

        companion object {
            fun fromLines(lines: String, fallbackName: String = randomName()): Template {
                val file = PlanetFile(lines)
                return Template(
                    file.planet.name.let { if (it.isEmpty()) fallbackName else it },
                    file.content
                )
            }

            fun fromLines(lines: List<String>, fallbackName: String = randomName()): Template =
                fromLines(lines.joinToString("\n"), fallbackName = fallbackName)

            fun random() = Template(name = randomName())
        }
    }
}