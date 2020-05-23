package de.robolab.server.data

import de.robolab.common.planet.ID
import de.robolab.server.model.ServerPlanet as SPlanet

class FilePlanetStore(val directory: String) : IPlanetStore {

    override suspend fun add(planet: SPlanet): ID? =
        ID("SUCH-RANDOM-VERY-ID-WOW")

    override suspend fun remove(planet: SPlanet): Boolean = false

    override suspend fun remove(id: ID): SPlanet? = null

    override suspend fun get(id: ID): SPlanet? = SPlanet()

    override suspend fun get(name: String): List<SPlanet> = listOf(SPlanet(), SPlanet())

    override suspend fun getIDs(): List<ID> {
        return listOf("123", "HELLO-WORLD").map { ID(it) }
    }

}