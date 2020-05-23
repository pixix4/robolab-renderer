package de.robolab.server.data

import de.robolab.common.planet.ID
import de.robolab.server.model.ServerPlanet as SPlanet

interface IPlanetStore {
    suspend fun add(planet: SPlanet): ID?
    suspend fun remove(planet: SPlanet): Boolean
    suspend fun remove(id: ID): SPlanet?

    suspend fun get(id: ID): SPlanet?
    suspend fun get(name: String): List<SPlanet>

    suspend fun getIDs(): List<ID>
}