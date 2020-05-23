package de.robolab.server.model

import de.robolab.common.planet.ID
import de.robolab.common.planet.Planet
import de.robolab.common.planet.randomName

class ServerPlanet(val name: String? = randomName()) {
    val id: ID = ID(name ?: randomName())
    val lines: MutableList<String> = mutableListOf()

    init {
        if (name != null)
            lines.add("#name: $name")
    }

    suspend fun lockLines(): Unit {}
    suspend fun lockLines(timeout: Int) {}
    fun unlockLines(): Unit {}
}