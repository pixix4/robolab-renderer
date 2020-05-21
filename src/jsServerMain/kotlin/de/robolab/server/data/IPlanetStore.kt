package de.robolab.server.data

import de.robolab.server.model.ID
import de.robolab.server.model.Planet

interface IPlanetStore {
    suspend fun add(planet: Planet):Boolean
    suspend fun remove(planet:Planet):Boolean
    suspend fun remove(id: ID):Planet?

    suspend fun get(name:String):List<Planet>


}