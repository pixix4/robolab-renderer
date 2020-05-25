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

    override suspend fun listPlanets(): List<Pair<ID,String>> {
        return listOf("123" to "Planet-456", "HELLO-WORLD" to "FooBarrisson").map { ID(it.first) to it.second }
    }

}