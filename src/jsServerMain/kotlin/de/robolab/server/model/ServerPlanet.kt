package de.robolab.server.model

import de.robolab.common.planet.ID
import de.robolab.common.planet.randomName

class ServerPlanet(val name: String? = randomName()) {
    val id: ID = (name ?: randomName()).toID()
    val lines: MutableList<String> = mutableListOf()

    init {
        if (name != null)
            lines.add("#name: $name")
    }

    suspend fun lockLines(): Unit {}
    suspend fun lockLines(timeout: Int) {}
    fun unlockLines(): Unit {}
}