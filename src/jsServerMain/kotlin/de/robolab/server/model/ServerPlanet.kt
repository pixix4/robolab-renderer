package de.robolab.server.model

import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.planet.randomName

class ServerPlanet(name: String = randomName(), id: String, lines: List<String> = listOf("#name: $name")) {

    constructor(info: ServerPlanetInfo, lines: List<String> = listOf("#name: ${info.name}")) : this(
        info.name,
        info.id,
        lines
    )

    companion object {
        fun random(): ServerPlanet = Template.random().let { it.withID(it.name) }
    }

    val info: ServerPlanetInfo = ServerPlanetInfo(id, name)
    val lines: MutableList<String> = lines.toMutableList()
    val name: String = info.name
    val id: String = info.id

    fun asTemplate(): Template = Template(name, lines)

    fun reparsed(): ServerPlanet {
        val file = PlanetFile(lines.joinToString("\n"))
        return ServerPlanet(
            file.planet.name.let { if (it.isEmpty()) name else it },
            id,
            lines
        )
    }

    suspend fun lockLines(): Unit {}
    suspend fun lockLines(timeout: Int) {}
    fun unlockLines(): Unit {}

    class Template(val name: String, lines: List<String> = listOf("#name: $name")) {
        val lines: MutableList<String> = lines.toMutableList()

        fun withID(id: String): ServerPlanet = ServerPlanet(name = name, id = id, lines = lines)

        fun reparsed(): Template = fromLines(lines, name)

        companion object {
            fun fromLines(lines: String, fallbackName: String = randomName()): Template {
                val file = PlanetFile(lines)
                return Template(
                    file.planet.name.let { if (it.isEmpty()) fallbackName else it },
                    file.content.split("""\r?\n""".toRegex()))
            }

            fun fromLines(lines: List<String>, fallbackName: String = randomName()): Template =
                fromLines(lines.joinToString("\n"), fallbackName = fallbackName)

            fun random() = Template(name = randomName())
        }
    }
}