package de.robolab.server.model

import de.robolab.common.planet.PlanetFile
import de.robolab.common.planet.utils.ServerPlanetInfo
import de.robolab.common.planet.utils.randomName
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.observe
import de.westermann.kobserve.property.property
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class ServerPlanet(info: ServerPlanetInfo, lines: String) {


    companion object {
        fun random(): ServerPlanet = Template.random().let { it.withID(it.name) }
    }

    val planetFile: PlanetFile = PlanetFile(lines)

    private val _fallbackName: ObservableProperty<String> = info.name.observe()

    val nameProp: ObservableValue<String> = property(planetFile.planetProperty, _fallbackName) {
        if (planetFile.planet.name.isEmpty()) _fallbackName.value else planetFile.planet.name
    }
    val name: String by nameProp

    val linesProp: ObservableValue<String> =
        property(planetFile.planetProperty) {
                planetFile.stringify()
        }
    val lines: String by linesProp

    val id: String = info.id

    val _lastModified: ObservableProperty<Instant> = info.lastModified.observe()
    val lastModifiedProp: ObservableValue<Instant> = _lastModified
    val lastModified: Instant by lastModifiedProp
    val _tagMapProp: ObservableProperty<Map<String,List<String>>> = info.tags.observe()
    val tagMapProp: ObservableValue<Map<String,List<String>>> = _tagMapProp
    val tagMap: Map<String, List<String>> by tagMapProp


    val infoProp: ObservableValue<ServerPlanetInfo> = property(nameProp, lastModifiedProp, tagMapProp) {
        ServerPlanetInfo(id, name, lastModified, tagMap)
    }
    val info by infoProp

    init {
        this.planetFile.planetProperty.onChange.addListener {
            _lastModified.set(Clock.System.now())
        }
        var previousModifiedInfo = this.info
        this.infoProp.onChange.addListener {
            val newValue = this.info
            if (previousModifiedInfo.withMTime(newValue.lastModified) != newValue) {
                previousModifiedInfo = newValue
                _lastModified.set(Clock.System.now())
            }
        }
        this.nameProp.onChange.addListener {
            if (this.name != this._fallbackName.value)
                this._fallbackName.set(this.name)
        }
    }

    fun asTemplate(): Template = Template(name, planetFile.stringify(), planetFile.planet.tags)

    suspend fun lockLines(): Unit {}
    suspend fun lockLines(timeout: Int) {}
    fun unlockLines(): Unit {}

    class Template(val name: String, var lines: String = "", val tags: Map<String,List<String>> = emptyMap()) {

        fun withID(id: String): ServerPlanet = ServerPlanet(ServerPlanetInfo(id, name, Clock.System.now(), tags), lines = lines)

        fun reparsed(): Template = fromLines(lines, name)

        companion object {
            fun fromLines(lines: String, fallbackName: String = randomName()): Template {
                val file = PlanetFile(lines)
                return Template(
                    file.planet.name.let { if (it.isEmpty()) fallbackName else it },
                    file.stringify(),
                    file.planet.tags
                )
            }

            fun fromLines(lines: List<String>, fallbackName: String = randomName()): Template =
                fromLines(lines.joinToString("\n"), fallbackName = fallbackName)

            fun random() = Template(name = randomName())
        }
    }
}
